# Concurrency Design Reference

## Concurrency Vocabulary

| Term | Description                                                                        |
|---|------------------------------------------------------------------------------------|
| **TOCTOU** | Time-of-check-to-time-of-use - gap between reading state and acting on it          |
| **Lock striping** | One lock per resource (not one global lock) - enables parallelism                  |
| **CAS** | Compare-And-Swap - CPU-level atomic update, no OS lock                             |
| **Backpressure** | Slowing producers when consumers are behind                                        |
| **Spurious wakeup** | Thread wakes from `await()` without being signalled — always use `while`, not `if` |
| **Write starvation** | Continuous readers block a waiting writer indefinitely                             |
| **Compound operation** | Two or more steps that must be atomic together (e.g., check-then-act)              |

---

## 1. Vending Machine

### Concurrency risks
- Two threads select the same item simultaneously - both see `quantity > 0`, both dispense
- Payment and dispensing must be atomic together (no partial state)

### Design
| What | How | Why |
|---|---|---|
| Per-item lock | `ConcurrentHashMap<itemId, ReentrantLock>` + `computeIfAbsent` | Parallel purchases of different items; serialize only same-item contention |
| State transition | `ReentrantLock` per item, held across check → deduct → dispense | Prevent TOCTOU between quantity check and deduction |
| Item inventory | `ConcurrentHashMap<itemId, Integer>` with `compute()` for deduction | Atomic read-modify-write on quantity |

### Key decisions
- **Why not `synchronized(this)`?** Serializes all item purchases through one lock — a purchase of Cola blocks a purchase of Water.
- **Why `computeIfAbsent` for the lock map?** Creating a lock entry itself has a race — `computeIfAbsent` is atomic; `get()` + `put()` is not.
- Per-item `ReentrantLock` over `synchronized`: you may want `tryLock()` to fail-fast if item is currently being processed rather than queuing up threads.

---

## 2. ATM Machine

### Concurrency risks
- Concurrent withdrawals from the same account — balance goes negative
- Concurrent transfers — deadlock if two transfers lock accounts in opposite order

### Design
| What | How | Why |
|---|---|---|
| Per-account lock | `ConcurrentHashMap<accountId, ReentrantLock>` | Only contend on same account — different accounts run in parallel |
| Withdrawal | Acquire account lock → check balance → deduct → release | Holds lock across the full check-then-act |
| Transfer (two accounts) | Always lock lower accountId first, then higher | Consistent lock ordering prevents deadlock |
| Balance | `BigDecimal` field, only ever mutated under the account lock | Financial precision; no `double` |

### Key decisions
- **Why consistent lock ordering for transfers?** Thread A locks account 1 then 2; Thread B locks account 2 then 1 → deadlock. Always acquire in the same order (e.g., by account ID) to break the cycle.
- **Why `ReentrantLock` over `synchronized`?** `tryLock(timeout)` on transfer — if you can't acquire both locks within a deadline, back off and retry rather than deadlock.
- **Avoid holding both locks longer than needed** — acquire, transfer, release immediately.

---

## 3. Meeting Room Scheduler

### Concurrency risks
- Two threads book the same room for overlapping slots simultaneously
- Both see the room as available, both confirm — double booking

### Design
| What | How | Why |
|---|---|---|
| Per-room lock | `ConcurrentHashMap<roomId, ReentrantLock>` + `computeIfAbsent` | Parallel bookings for different rooms; serialize only same-room contention |
| Booking check + insert | Held under room lock | Atomic: check availability → insert booking → confirm |
| Booking store | `ConcurrentHashMap<roomId, List<Booking>>` | Per-room booking list, accessed only under room lock |
| Timeout on lock | `tryLock(500ms)` | Avoid blocking indefinitely under contention — return "try again" to caller |

### Key decisions
- **Why per-room lock, not per-slot?** Overlap detection requires scanning all bookings for a room — need to hold the room-level lock during the scan.
- **Why `tryLock` with timeout?** In a booking UI, blocking a user's request indefinitely is worse than returning a "room busy" response quickly.

---

## 4. Logging Framework

### Concurrency risks
- Many threads logging simultaneously — log entries interleaved or lost
- Logger configuration changed mid-flight — appenders list mutated during iteration

### Design
| What | How | Why |
|---|---|---|
| Log queue | `BlockingQueue<LogEntry>` (bounded `ArrayBlockingQueue`) | Async decoupling — callers don't block on I/O; backpressure if writer falls behind |
| Appender list | `CopyOnWriteArrayList<Appender>` | Appenders added/removed rarely; iterated on every log call — safe iteration without locking |
| Queue full policy | `offer()` with timeout (not `put()`) | Don't block the calling thread indefinitely on a full queue — drop or fallback to sync |
| Background writer | Single consumer thread draining the queue | Serializes disk/network writes without locking |
| Log level check | `volatile int logLevel` | Cheap visibility — level changes are rare, reads are constant |

### Key decisions
- **Why `BlockingQueue` over `ConcurrentLinkedQueue`?** You want the writer thread to block/wait when the queue is empty rather than spin-poll.
- **Why bounded queue?** An unbounded queue hides the fact that your I/O writer is too slow — bounded queue surfaces backpressure.
- **Why `CopyOnWriteArrayList` for appenders?** You need to iterate appenders on every log call. Locking iteration would contend with every logging thread. Since appender config changes are rare, copy-on-write is ideal.

---

## 5. File System

### Concurrency risks
- Concurrent reads and writes to the same file or directory node
- Directory structure modified (move/delete) while another thread traverses it

### Design
| What | How | Why |
|---|---|---|
| Per-node lock | `ReentrantReadWriteLock` per `FileNode` / `DirectoryNode` | Multiple readers (reads/stats) in parallel; exclusive writer (write/rename/delete) |
| Directory structure | `ConcurrentHashMap<name, Node>` as children map | Thread-safe child lookup without locking the whole tree |
| Rename / move | Lock source parent + destination parent + node | Multi-lock operation — acquire in consistent order (e.g., by node ID) to avoid deadlock |
| File content reads | Read lock on node | Multiple threads can read same file concurrently |
| File content writes | Write lock on node | Exclusive — no readers during write |

### Key decisions
- **Why `ReadWriteLock` per node instead of a global lock?** A global lock serializes all file system operations — unrelated files would contend. Per-node gives parallelism across the tree.
- **Why consistent lock ordering on rename?** Same deadlock risk as ATM transfers — two renames involving the same nodes in opposite order.
- **`ReentrantReadWriteLock` over `StampedLock` here?** File systems have non-trivial write frequency — `StampedLock`'s optimistic read benefit is marginal; its non-reentrancy adds risk.

---

## 6. Parking Lot

### Concurrency risks
- Two vehicles assigned the same spot simultaneously
- Spot availability count goes out of sync with actual assignments

### Design
| What | How | Why |
|---|---|---|
| Per-spot lock | `ConcurrentHashMap<spotId, ReentrantLock>` + `computeIfAbsent` | Parallel assignments to different spots |
| Available spot tracking | `AtomicInteger` per floor/type | Fast, lock-free count checks without global lock |
| Spot assignment | Acquire spot lock → mark occupied → release | Atomic check-and-assign under spot lock |
| Spot search | Iterate `ConcurrentHashMap` of spots — no lock needed for scan | Reads are non-blocking; only lock when you're about to assign |

### Key decisions
- **Why `AtomicInteger` for available count?** You only need an approximate count for "is there space?" — the definitive check happens under the spot lock anyway. Don't serialize spot searches through a lock.
- **Why not lock the entire floor?** A vehicle parking on Floor 1 / Spot A should not block a vehicle parking on Floor 1 / Spot Z.
- **TOCTOU note:** Always re-verify the spot is still free after acquiring its lock — the scan result may be stale.

---

## 7. Splitwise

### Concurrency risks
- Concurrent expense additions — running balances between users go inconsistent
- Concurrent settlements — a settlement and an expense addition race on the same user pair

### Design
| What | How | Why |
|---|---|---|
| Per-user-pair lock | `ConcurrentHashMap<pairKey, ReentrantLock>` | Only serialize operations on the same user pair |
| Balance map | `ConcurrentHashMap<pairKey, BigDecimal>` with `compute()` | Atomic balance update per pair |
| Pair key | Always `min(userId1, userId2) + "_" + max(...)` | Canonical key — avoids two maps for A→B and B→A |
| Expense split | Lock pair → update balances → release | Holds lock across multi-user split calculation |

### Key decisions
- **Why canonical pair key?** Without it, a payment from A to B and B to A could use different keys, creating separate entries and double-counting.
- **Why `BigDecimal`?** Split amounts involve division — floating point precision loss is unacceptable in financial balances.
- **Group expenses (3+ people):** Lock each affected pair in sorted order to prevent deadlock across concurrent group expenses.

---

## 8. Food Delivery System

### Concurrency risks
- Order assigned to a delivery agent who just became unavailable
- Order status transitions race (e.g., two threads try to mark order as PICKED_UP simultaneously)

### Design
| What | How | Why |
|---|---|---|
| Order state transition | `ReentrantLock` per order + state check under lock | Atomic: validate current state → transition to next |
| Agent availability | `ConcurrentHashMap<agentId, AtomicBoolean>` | Lock-free availability flip with CAS |
| Agent assignment | `compareAndSet(true, false)` on agent's available flag | Atomic claim — only one order can claim an agent |
| Order queue | `BlockingQueue<Order>` per zone/partition | Async order intake with backpressure |
| Active orders | `ConcurrentHashMap<orderId, Order>` | Fast lookup for status updates |

### Key decisions
- **Why `AtomicBoolean` + CAS for agent assignment?** You need exactly-once assignment — `compareAndSet(available=true, false)` means only one thread succeeds, all others see `false` and move on.
- **Why per-order lock for state transitions?** State machine correctness requires the check-then-transition to be atomic. Without it, two events could both see `PREPARING` and both try to transition to different next states.

---

## 9. Inventory + Order Management

### Concurrency risks
- Overselling — two orders both reserve the last unit
- Order and inventory updates must be consistent (reserve inventory → confirm order atomically)

### Design
| What | How | Why |
|---|---|---|
| Per-product lock | `ConcurrentHashMap<productId, ReentrantLock>` | Parallel orders for different products |
| Inventory | `ConcurrentHashMap<productId, AtomicInteger>` | Fast reads; writes under product lock |
| Reservation | Acquire product lock → check stock → decrement → create order → release | Entire check-reserve-confirm under one lock |
| Order store | `ConcurrentHashMap<orderId, Order>` | Lock-free order lookup |
| Timeout | `tryLock(timeout)` on product lock | Don't block order threads indefinitely during flash sales |

### Key decisions
- **Why not just `AtomicInteger.decrementAndGet()` for inventory?** Decrement is atomic, but check-then-decrement is not — you'd need a CAS loop. A `ReentrantLock` is cleaner when the critical section spans multiple operations (check + decrement + order creation).
- **Flash sale scenario:** High contention on popular products — `tryLock(timeout)` lets you fail fast and return "sold out" rather than queuing thousands of threads.

---

## 10. LRU Cache

### Concurrency risks
- Concurrent reads and writes to the cache map and the doubly-linked list
- Eviction and insertion race — evicting a node that's simultaneously being promoted

### Design
| What | How | Why |
|---|---|---|
| Single `ReentrantReadWriteLock` | Read lock for `get()`; write lock for `put()` and eviction | Cache is read-heavy — multiple readers can look up simultaneously |
| Internal structure | `LinkedHashMap` (access-order) or manual HashMap + doubly-linked list, accessed only under lock | LRU ordering requires moving nodes — not thread-safe by itself |
| `get()` | Acquire read lock → lookup → **upgrade to write lock to promote node** | Promotion (move to head) mutates the list — needs write lock |

### ⚠️ Read-lock upgrade caveat
`ReentrantReadWriteLock` does **not** support lock upgrade (read → write). Pattern:

```
// get() — must use write lock if you promote on access
writeLock.lock()
  lookup node
  move to head (LRU promotion)
  return value
writeLock.unlock()
```

Or: separate "dirty read" (no promotion, read lock) from "touch" (write lock). Depends on whether strict LRU ordering matters.

### Key decisions
- **Why not `ConcurrentHashMap` alone?** The LRU ordering (doubly-linked list for O(1) eviction) is not thread-safe. The map and the list must be updated atomically.
- **Why a single lock vs. per-bucket?** The eviction policy touches both the map and the list together — you can't stripe these independently.
- **`synchronized` vs `ReentrantReadWriteLock`?** If reads are dominant and you accept write-lock-on-get, `ReadWriteLock` wins. If writes are frequent, `synchronized` is simpler with negligible difference.

---

## 11. Rate Limiter (Token Bucket)

### Concurrency risks
- Two requests both check token count simultaneously, both see `tokens >= 1`, both consume — over-limit requests get through
- Token refill races with consumption

### Design
| What | How | Why |
|---|---|---|
| Per-user bucket | `ConcurrentHashMap<userId, TokenBucket>` + `computeIfAbsent` | Per-user limiting without global lock |
| Token check + consume | `ReentrantLock` per `TokenBucket` | Check-then-decrement must be atomic |
| Token count | `double tokens` field inside `TokenBucket`, only mutated under its lock | Fractional tokens needed for sub-second refill rates |
| Refill | Lazy refill on each `allow()` call using `Instant.now()` delta | No background thread needed — refill computed at request time |
| Global rate limit | Single `TokenBucket` with one lock | Same pattern, shared across all users |

### Key decisions
- **Why not `AtomicInteger` for tokens?** Refill + consume is a multi-step operation (compute elapsed time → add tokens → check → decrement) — not expressible as a single CAS.
- **Why lazy refill?** Avoids a background scheduler thread per user. On each request, compute how many tokens to add since the last request timestamp. Simpler and scales better.
- **Strategy pattern note:** The locking design doesn't change between Token Bucket and Sliding Window — the `TokenBucket` class is swappable behind a `RateLimitStrategy` interface.

---

## 12. Elevator System

### Concurrency risks
- Multiple elevators picking up the same floor request simultaneously
- Elevator state (floor, direction, door status) mutated by both the scheduler and the elevator thread

### Design
| What | How | Why |
|---|---|---|
| Per-elevator lock | `ReentrantLock` per `Elevator` | Each elevator's state (floor, direction, queue) is independent |
| Request assignment | Scheduler holds elevator lock during "find best elevator + assign request" | Atomic: evaluate + assign without another thread stealing the elevator |
| Elevator state | Enum (`IDLE`, `MOVING_UP`, `MOVING_DOWN`, `DOOR_OPEN`) — transitioned under elevator lock | State machine correctness |
| Floor requests | `ConcurrentSkipListMap<floor, Request>` per elevator | Ordered by floor for efficient next-stop calculation; concurrent adds from scheduler |
| Pending requests | `BlockingQueue<Request>` for unassigned requests | Scheduler thread consumes and dispatches |

### Key decisions
- **Why `ConcurrentSkipListMap` for the elevator's stop queue?** You need ordered traversal (next floor up/down) with concurrent inserts from the scheduler. A sorted structure beats a heap here because you query by range, not just min.
- **Why `BlockingQueue` for incoming requests?** The scheduler thread should block-wait for new requests rather than spin-poll.
- **Why per-elevator lock?** Elevator A moving to Floor 3 should not block Elevator B accepting a request for Floor 7.

---

## 13. Pub/Sub System

### Concurrency risks
- Subscriber list mutated (subscribe/unsubscribe) while an event is being dispatched to it
- Slow subscriber blocks dispatch to other subscribers on the same topic

### Design
| What | How | Why |
|---|---|---|
| Subscriber list per topic | `CopyOnWriteArrayList<Subscriber>` | Dispatch iterates the list — subscribe/unsubscribe are rare; iteration is constant |
| Topic map | `ConcurrentHashMap<topic, CopyOnWriteArrayList<Subscriber>>` + `computeIfAbsent` | Atomic topic creation |
| Dispatch | Async — per-subscriber `BlockingQueue<Event>` or thread pool | Slow subscriber doesn't block the publisher or other subscribers |
| Event queue (if async) | `BlockingQueue<Event>` per subscriber | Subscriber consumes at its own pace; backpressure if it falls behind |

### Key decisions
- **Why `CopyOnWriteArrayList` over `ReadWriteLock` + `ArrayList`?** During dispatch you iterate and call subscriber handlers — potentially slow. Holding a read lock during slow handler execution would block any subscribe/unsubscribe. Copy-on-write lets dispatch iterate a snapshot freely.
- **Why async dispatch?** Synchronous dispatch means one slow or failing subscriber blocks all others on the same topic. Give each subscriber its own queue or use a thread pool.
- **`computeIfAbsent` for topic creation** — two threads subscribing to a new topic simultaneously must not create two separate lists.

---

## 14. In-Memory Key-Value Store (Redis-like)

### Concurrency risks
- Concurrent reads and writes to the same key
- TTL expiry and a write race — key deleted by expiry thread simultaneously with a client update

### Design
| What | How | Why |
|---|---|---|
| Main store | `ConcurrentHashMap<String, ValueWrapper>` | High-throughput concurrent reads and writes |
| Per-key lock (for complex ops) | `ConcurrentHashMap<key, ReentrantLock>` + `computeIfAbsent` | Atomic multi-step ops: `INCR`, `LPUSH`, `SETNX` |
| TTL store | `ConcurrentSkipListMap<expiryTime, key>` | Efficiently find all expired keys — sorted by expiry timestamp |
| Expiry | Background thread polls `headMap(now)` of TTL store and removes | Non-blocking scan; `ConcurrentSkipListMap` handles concurrent inserts |
| Simple GET/SET | `ConcurrentHashMap.get()` / `put()` directly — no extra lock | These are already atomic enough |

### Key decisions
- **Why `ConcurrentSkipListMap` for TTL?** You need "give me all keys expiring before time T" — a sorted map's `headMap(T)` is O(log n). A heap or list would need full scan or locking.
- **Why per-key lock only for complex ops?** `GET`/`SET` on `ConcurrentHashMap` are already atomic. Only `INCR` (read + increment + write), `SETNX` (check + set), and `LPUSH` (mutate list value) need extra atomicity.
- **TTL race:** Expiry thread deletes a key at the same moment a client writes it. Use `remove(key, value)` on the map (conditional remove) or hold the per-key lock during expiry deletion.

---

## 15. Movie Ticket Booking System

### Concurrency risks
- Same seat booked by two users simultaneously (double booking)
- Payment processed but seat already taken by the time it commits

### Design
| What | How | Why |
|---|---|---|
| Per-seat lock | `ConcurrentHashMap<seatId, ReentrantLock>` + `computeIfAbsent` | Only contend on the same seat — parallel bookings for different seats |
| Booking flow | Acquire seat lock → check availability → mark RESERVED → trigger payment → confirm or release | Hold lock for the entire reservation window |
| Seat state | Enum (`AVAILABLE`, `RESERVED`, `BOOKED`) — transitioned under seat lock | Explicit states prevent partial transitions |
| Timeout on lock | `tryLock(500ms)` | Seat is being processed — return "try again" rather than blocking user |
| Show inventory | `ConcurrentHashMap<showId, Map<seatId, SeatState>>` | Per-show seat maps, looked up concurrently |

### Key decisions
- **Why hold the lock through payment?** If you release after `RESERVED` and re-acquire for `BOOKED`, another thread could slip in between. Hold the lock for the full reservation transaction.
- **Practical trade-off:** In real systems, payment goes to an external service — you wouldn't hold a JVM lock across a network call. For LLD interview scope, holding the lock is acceptable and expected. If the interviewer pushes, mention a timeout (`tryLock`) and idempotency key as the production mitigation.
- **Why not a global show lock?** A booking for Seat A1 in Show 1 should not block a booking for Seat G7 in the same show.

---

## 16. Digital Wallet / Banking System

### Concurrency risks
- Concurrent debits exceed balance (overdraft)
- Transfer between two wallets deadlocks (same risk as ATM)
- Transaction ledger entries race — two threads append simultaneously

### Design
| What | How | Why |
|---|---|---|
| Per-wallet lock | `ReentrantLock` per `Wallet` | Serialize operations on the same wallet |
| Transfer (two wallets) | Lock lower walletId first, then higher (consistent ordering) | Prevents deadlock between concurrent transfers |
| Balance | `BigDecimal` field, only mutated under wallet lock | Financial precision |
| Debit | Acquire lock → check balance → deduct → append ledger entry → release | Atomic: check-then-deduct-then-record |
| Ledger | `CopyOnWriteArrayList<Transaction>` per wallet or `ConcurrentLinkedQueue` | Ledger is append-only and read for statements (read-heavy) |
| Idempotency | `ConcurrentHashMap<txnId, TransactionResult>` | Retry-safe — same transaction ID returns same result |

### Key decisions
- **Why consistent lock ordering for transfers?** Wallet A → B and B → A running concurrently deadlock without it. Always acquire `min(walletId)` first.
- **Why `ReentrantLock` over `synchronized` for wallets?** `tryLock(timeout)` — if you can't acquire both wallet locks within a deadline, abort and retry with a new transaction ID rather than deadlock.
- **Why idempotency map?** In payment systems, retries are common (network timeouts, client retries). The map ensures a retried transaction doesn't debit twice.
- **Ledger append-only pattern:** Never update ledger entries — only append. This means ledger reads don't contend with each other; only appends need coordination.

---

## Cross-Cutting Patterns (Appear in Multiple Problems)

### Per-resource lock registry
```
ConcurrentHashMap<resourceId, ReentrantLock> + computeIfAbsent
```
Used in: Parking Lot, ATM, Meeting Room, Booking System, Inventory, KV Store, Vending Machine.
The pattern is always the same — never `get()` + `put()` to create a lock entry.

### Consistent lock ordering (deadlock prevention)
Always acquire multiple locks in a canonical order (e.g., sorted by ID).
Used in: ATM (accounts), File System (rename), Splitwise (user pairs), Wallet (transfer), Elevator (multi-resource).

### Atomic state transitions
Acquire lock → validate current state → transition → release. Never check state outside the lock.
Used in: Food Delivery (order states), Elevator (direction/floor), Booking (seat states), Vending Machine (item availability).

### Async decoupling with `BlockingQueue`
Caller enqueues work and returns immediately; background thread processes.
Used in: Logging Framework, Food Delivery (order intake), Pub/Sub (subscriber dispatch), Elevator (request queue).

### `CopyOnWriteArrayList` for iterated-but-rarely-mutated lists
Used in: Pub/Sub (subscribers), Logging (appenders), Wallet (ledger reads).

### `ConcurrentSkipListMap` for sorted concurrent data
Used in: Elevator (stop queue), KV Store (TTL), Pub/Sub (event ordering if needed).

### `computeIfAbsent` for atomic lazy initialization
Any time you build a per-resource map entry (lock, bucket, list) — never use `get()` + `putIfAbsent()` separately.

---

## At-a-Glance: What Each Problem Reaches For

| Problem | Primary Lock | Primary Structure | Key Pattern |
|---|---|---|---|
| Vending Machine | `ReentrantLock` per item | `ConcurrentHashMap` | Per-resource lock, `computeIfAbsent` |
| ATM | `ReentrantLock` per account | `ConcurrentHashMap` | Consistent lock ordering (transfer) |
| Meeting Room | `ReentrantLock` per room | `ConcurrentHashMap` | `tryLock` timeout, per-resource |
| Logging | — | `BlockingQueue`, `CopyOnWriteArrayList` | Async decoupling, bounded queue |
| File System | `ReadWriteLock` per node | `ConcurrentHashMap` (children) | Read-heavy per-node, consistent ordering |
| Parking Lot | `ReentrantLock` per spot | `ConcurrentHashMap`, `AtomicInteger` | Per-resource, approximate count |
| Splitwise | `ReentrantLock` per pair | `ConcurrentHashMap` | Canonical pair key, `BigDecimal` |
| Food Delivery | `ReentrantLock` per order | `AtomicBoolean` (agent), `BlockingQueue` | CAS agent claim, state transitions |
| Inventory | `ReentrantLock` per product | `ConcurrentHashMap` | `tryLock` for flash sales |
| LRU Cache | `ReadWriteLock` (single) | `LinkedHashMap` under lock | No lock upgrade — write lock on get |
| Rate Limiter | `ReentrantLock` per bucket | `ConcurrentHashMap` | Lazy refill, per-user buckets |
| Elevator | `ReentrantLock` per elevator | `ConcurrentSkipListMap`, `BlockingQueue` | Ordered stop queue, async intake |
| Pub/Sub | — | `CopyOnWriteArrayList`, `BlockingQueue` | Snapshot iteration, async dispatch |
| KV Store | `ReentrantLock` per key (complex ops) | `ConcurrentHashMap`, `ConcurrentSkipListMap` | TTL via sorted map, conditional ops |
| Movie Booking | `ReentrantLock` per seat | `ConcurrentHashMap` | Hold lock through payment, `tryLock` |
| Digital Wallet | `ReentrantLock` per wallet | `ConcurrentHashMap` (idempotency) | Consistent ordering, idempotency map |