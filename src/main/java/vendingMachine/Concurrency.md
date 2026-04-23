# Vending Machine — Concurrency Design Notes

---

## Step 1: Threat Modelling — What's Actually Shared?

Before writing any code, identify **shared mutable state** first.
Jumping straight to `synchronized` everywhere is a red flag.

In a vending machine, concurrent access means:
- Multiple customer threads hitting the same machine simultaneously
- An operator thread restocking while a customer is mid-transaction

| Shared State | Location | Risk |
|---|---|---|
| `currentState` | `VendingMachine` | Two threads both read `IdleState`, both start a session |
| `transaction` | `VendingMachine` | Read/written across state transitions |
| `coins` (EnumMap) | `CoinInventory` | Concurrent dispense + refund = corrupted counts |
| `products` (HashMap) | `ProductInventory` | Operator adds product while customer selects → `ConcurrentModificationException` |
| `quantity` (int) | `Product` | TOCTOU — check then decrement are two separate operations |

`ChangeCalculationStrategy` — **not shared mutable state**. Stateless. Safe.

`Transaction` — **not shared between threads**. One transaction belongs to one customer session. No concurrency concern.

---

## Step 2: The Problems, Ranked by Severity

### Problem 1 — State Transition Race (most critical)

```java
// Thread A and Thread B both call startSession() simultaneously
// Both read currentState = IdleState
// Both call IdleState.startSession()
// Both transition to ProductSelectionState
// Now two transactions are running simultaneously on one machine
```

A vending machine is a **single-server resource** — only one customer can use it at a time.
The fix is a **machine-level lock that serializes the entire customer interaction**.

### Problem 2 — Product Quantity TOCTOU

```
selectProductToPurchase(): checks quantity > 0    ← Thread A passes check
                                                  ← Thread B passes check
executeDispense(): calls reduceQuantity()         ← Both decrement → quantity = -1
```

**TOCTOU = Time Of Check To Time Of Use.**
The race is between the *check* and the *decrement* — not just the decrement alone.

### Problem 3 — CoinInventory Corruption

`addCoins` and `removeCoins` do a read-then-write with no atomicity.
Concurrent dispense and refund can interleave and produce wrong coin counts.

### Problem 4 — ProductInventory HashMap

`HashMap` is not thread-safe.
Operator calling `addProduct` while customer calls `getProduct` can throw `ConcurrentModificationException`.
Fix: replace with `ConcurrentHashMap`.

---

## Step 3: The Right Tool for Each Problem

| Problem | Wrong Approach | Right Approach | Why |
|---|---|---|---|
| State transition race | `synchronized` on every method | `ReentrantLock` on the machine, held for the **entire session** | A session spans multiple method calls — you need a lock that outlives a single method |
| Product quantity TOCTOU | `synchronized` on `reduceQuantity` alone | `synchronized` on the check-and-decrement **together** | The race is between the check and the decrement, not just the decrement |
| CoinInventory corruption | `synchronized` on every method | `ReentrantReadWriteLock` | Multiple threads can read counts concurrently; writes (add/remove) need exclusive access |
| ProductInventory HashMap | `synchronized` | `ConcurrentHashMap` | Standard replacement, no custom logic needed |

---

## Step 4: The Key Insight — Compound Actions

If you only lock `CoinInventory` and `ProductInventory` independently, you still have a window:

```
Thread A checks product quantity  (ProductInventory lock released)
Thread B checks product quantity  (both pass the check)
Both proceed to dispense
```

Resources need to be locked **together** for the dispense to be atomic.
This is a **compound action problem** — you need a single machine-level lock
covering the entire `selectProduct → insertCoins → dispense` sequence.

---

## Step 5: Lock Design

### Machine-Level Lock: `ReentrantLock`

```
Why ReentrantLock over synchronized?

synchronized: lock is released when the method returns
ReentrantLock: lock can be acquired in one method and released in another

A customer session spans: startSession() → selectProduct() → insertCoins() → dispenseProduct()
These are FOUR separate method calls. synchronized cannot hold across them.
ReentrantLock held from startSession() and released after dispenseProduct() or cancel().
```

### CoinInventory: `ReentrantReadWriteLock`

```
ReadWriteLock allows:
  - Multiple threads to READ simultaneously (getCount for change calculation)
  - Only ONE thread to WRITE at a time (addCoins, removeCoins)
  - No reads during a write

Why not just synchronized?
  synchronized would block all reads even when no write is happening.
  ReadWriteLock gives better throughput when reads are frequent.
```

### Product Quantity: `synchronized` on the compound check+decrement

```
The check (quantity <= 0) and the decrement (quantity--) must be atomic together.
Synchronizing only reduceQuantity() still leaves a window between
selectProductToPurchase() and executeDispense().
Both must run under the same machine-level lock.
```

---

## Step 6: Final Lock Architecture

```
VendingMachine
└── ReentrantLock machineLock
      Acquired: startSession()
      Released: after dispenseProduct() completes OR cancel() completes
      Covers: the entire customer session — all state transitions, dispense, refund

CoinInventory
└── ReentrantReadWriteLock coinLock
      readLock:  getCount()
      writeLock: addCoins(), removeCoins()

ProductInventory
└── ConcurrentHashMap (replaces HashMap)
      Thread-safe for concurrent get/put without explicit locking

Product.quantity
└── Protected by the machine-level lock
      (selectProductToPurchase + reduceQuantity both run under machineLock)
```

---

## Summary: What Each Lock Protects

```
┌─────────────────────────────────────────────────────────────┐
│  machineLock (ReentrantLock)                                │
│                                                             │
│  Protects: currentState, transaction, Product.quantity      │
│  Scope: entire customer session                             │
│  Acquired: startSession()                                   │
│  Released: dispenseProduct() finally / cancel() finally     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  coinLock (ReentrantReadWriteLock)                          │
│                                                             │
│  readLock:  getCount()      — concurrent reads OK           │
│  writeLock: addCoins()      — exclusive                     │
│             removeCoins()   — exclusive                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  ConcurrentHashMap                                          │
│                                                             │
│  Protects: products map in ProductInventory                 │
│  No explicit locking needed — internal segment locking      │
└─────────────────────────────────────────────────────────────┘
```

---

## Common Mistakes to Avoid in Interviews

**Mistake 1: Synchronizing only `reduceQuantity()`**
The TOCTOU race is between `selectProductToPurchase()` (the check) and `reduceQuantity()` (the act).
Synchronizing only one half doesn't fix the race.

**Mistake 2: Using `synchronized` for session-spanning operations**
`synchronized` releases the lock when the method returns.
A customer session spans 4+ method calls — you need `ReentrantLock` held across them.

**Mistake 3: Calling `lock()` without `finally { unlock() }`**
If an exception is thrown between `lock()` and `unlock()`, the lock is never released.
The machine is permanently stuck. Always unlock in `finally`.

**Mistake 4: Using `lock()` instead of `tryLock()` in `startSession()`**
`lock()` blocks indefinitely — the customer thread just hangs.
`tryLock()` fails fast and lets you give the customer a meaningful error message.

**Mistake 5: Over-synchronizing**
Putting `synchronized` on every method of every class is not thread safety — it's just
serialization with extra steps, and it often creates deadlock risk. Match the lock
granularity to the actual shared state and the actual compound actions.