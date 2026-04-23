# ATM — Concurrency Discussion

## Interview Stance

> Not expected to implement. Expected to identify, articulate, and scope.

The two magic sentences:
- *"I'm keeping this single-threaded for interview scope."*
- *"In production, here's where I'd add synchronization and why."*

---

## Issue 1 — `NoteDispenser.availableNotes` (ATM Layer)

### The Problem
`canDispense` and `dispense` are two separate calls with no atomicity guarantee.

```
Thread A (Session 1): canDispense(200) → true
Thread B (Session 2): canDispense(200) → true   ← both pass
Thread A:             dispense(200)              ← notes decremented
Thread B:             dispense(200)              ← over-dispenses!
```

`availableNotes` is shared mutable state across all sessions on the same ATM.

### Why This One Is Yours To Fix
This lives entirely in the ATM layer — no database involved. You own it.

### Fix: `synchronized` on the validate-and-dispense pair

```java
public synchronized boolean tryDispense(int amount) {
    if (!canDispense(amount)) return false;
    dispense(amount);
    return true;
}
```

Or use `AtomicInteger` for `availableNotes` if only the decrement needs protection (weaker, but works if `canDispense` logic is simple).

**`synchronized` is the right answer here** — the check-then-act must be atomic.

### What To Say In The Interview
> "`availableNotes` is shared mutable state. Two concurrent withdrawals could both pass `canDispense` and then both dispense, over-drawing the cassette. I'd make `tryDispense` a synchronized method so the check-and-act is atomic."

---

## Issue 2 — `BankService.debitAmount` (Database Layer)

### The Problem
Same TOCTOU structure, but at the account balance level.

```
Session A: getBalance()  → $500
Session B: getBalance()  → $500   ← both see $500
Session A: debit($500)   → balance = $0
Session B: debit($500)   → balance = -$500  ← overdraft!
```

### Why This One Is NOT Yours To Fix
In production, `BankService` is a thin client over a real bank backend. The fix lives at the **database layer**, not the ATM layer:

- **Pessimistic locking**: `SELECT ... FOR UPDATE` on the account row
- **Optimistic locking**: version column on the account, retry on conflict
- **Atomic DB operation**: `UPDATE account SET balance = balance - :amount WHERE balance >= :amount`

Trying to fix this with `synchronized` in Java is wrong — it only helps within a single JVM instance. A real system has multiple ATM nodes.

### What To Say In The Interview
> "The balance check and debit are two separate operations — classic TOCTOU. In production this is a database concern: either a row-level lock or an atomic conditional update. Adding `synchronized` in the ATM layer would only work for a single-node deployment, which isn't realistic."

---

## The Distinction That Impresses Interviewers

| Issue | Layer | Fix |
|---|---|---|
| `availableNotes` over-dispense | ATM / in-process | `synchronized` on check-and-act |
| Account balance overdraft | Database / distributed | Row lock or atomic DB update |

Knowing *which layer owns which problem* is the senior signal. A junior says "add `synchronized` everywhere." A senior says "this one needs a DB lock, and here's why Java sync doesn't help there."

---

## What To Skip (Out Of Scope For LLD)

- `ReentrantReadWriteLock` on `BankService` maps — overkill, not asked
- `ConcurrentHashMap` for `accounts` / `cards` — fine to mention in passing, not worth implementing
- Session isolation between two cards on the same ATM — ATMContext is per-session by design, not shared

---

## The 20-Second Version (Memorize This)

> "I've kept this single-threaded for scope. Two issues worth noting: `availableNotes` in `NoteDispenser` needs a synchronized check-and-act to prevent over-dispensing across concurrent sessions. For account balance, that's a database concern — a row lock or atomic conditional update — not something the ATM layer should own with Java synchronization."