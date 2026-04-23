# Synchronized (Intrinsic Lock)

```
// Method-level
public synchronized void withdraw(int amount) {
    balance -= amount;
}

// Block-level (preferred — smaller critical section)
synchronized (this) {
    balance -= amount;
}
```

### What is does?
* Acquires the intrinsic monitor lock on enter, releases on exit (even on exception)
* Reentrant by default - same thread can re-enter without deadlocking itself
* Block-level or method-level locking

### When to use?
* Simple critical sections & low contention
* No need for timeout, interrupt, or, fairness
* Correctness > performance

### Pros
* Strong mutual exclusion
* Simple, hard to misuse - since JVM handles unlock
* JVM optimizations (biased locking, lock elision) make it very fast at low contention

### Cons
* Thread gets blocked indefinitely - no timeout, no tryLock
* Not interruptible while waiting
* No fairness guarantee -> thread starvation is possible
* Poor scalability under contention

### Interview tip
I'd start with synchronized for simplicity and only upgrade to ReentrantLock if I need timeout, tryLock, or fairness - not just because it sounds more sophisticated.

---

# ReentrantLock (Explicit Lock)
```
private final ReentrantLock lock = new ReentrantLock();

// Basic usage — always unlock in finally
lock.lock();
try {
    balance -= amount;
} finally {
    lock.unlock();  // Must always unlock, or you get a deadlock
}

// Non-blocking: try and move on
if (lock.tryLock()) {
    try { /* do work */ }
    finally { lock.unlock(); }
} else {
    // fallback — pick another seat, retry, fail fast
}

// Timeout: don't block forever
if (lock.tryLock(2, TimeUnit.SECONDS)) {
    try { /* do work */ }
    finally { lock.unlock(); }
}

// Interruptible: allow thread to be cancelled while waiting
lock.lockInterruptibly();

// Fair lock: FIFO ordering, prevents starvation
private final ReentrantLock fairLock = new ReentrantLock(true);
```

### What is does?
* Same mutual exclusion as synchronized, but with explicit control (manual lock management).
* Reentrant: same thread can acquire multiple times (tracks hold count internally).
* unlock() decrements hold count; lock is released when count hits 0
* Internal working:
  * Under the hood, the JVM maintains:
    * state -> lock count
    * owner -> thread holding the lock
    * a queue of waiting threads
  * Flow:
    * Thread tries to acquire lock: ```lock.lock()```
      * If state == 0, lock is free -> take it
      * Else:
        * If same thread -> increment count
        * Else -> go to queue and wait
    * Once the lock is held, only the owner can proceed, others are parked (blocked efficiently, not busy waiting)
    * Unlock the lock: ```lock.unlock()```
      * Decrement count
      * If count == 0
        * release lock
        * wake up next thread in queue

### When to use?
* You need more control over locking behaviour, and not just mutual exclusion like Synchronized eg:
  * tryLock, timeout, interruptibility, fairness
* Complex locking flows (multiple locks, retry strategies, fallback paths)
* Avoiding indefinite blocking (booking systems, payment flows)

### Pros
* Fine-grained control over lock acquisition
* tryLock() enables fallback logic without blocking

### Cons
* Must manually unlock the lock (otherwise deadlock)

### Comparison with Synchronized:
* Both Synchronized and ReentrantLock do:
  * Mutual exclusion 
  * Blocking
  * Reentrancy
* So, use ReentrantLock over Synchronized when following are required:
  * Control over lock acquisition
    * Synchronized:
      * synchronized(lock) {
        // you're stuck here until you get the lock
        }
      * If lock is busy -> thread waits forever
      * No alternative path
    * ReentrantLock:
      * if (lock.tryLock()) {
        // got lock
        } else {
        // do something else
        }
      * Now, we can Skip work, Retry later, or, Fail fast
  * Timeouts
    * lock.tryLock(2, TimeUnit.SECONDS);
    * Synchronized - Thread waits indefinitely
    * ReentrantLock - You control waiting
    * Example: In payment system, ReentrantLock can timeout, retry, or, fail fast
  * Interruptibility
    * lock.lockInterruptibly();
    * Thread can be interrupted while waiting
    * But with synchronized, thread is stuck, no escape.
  * Fairness
    * new ReentrantLock(true); -> Threads get lock in FIFO order
    * For synchronized, JVM decides randomly, so thread starvation is possible
  * Manual control
    * Synchronized: JVM handles it automatically
    * ReentrantLock: You decide when to lock & when to unlock

### When is Synchronized better than ReentrantLock
* JVM optimizations make it very fast in low contention
* If you don’t need:
  * timeout
  * tryLock
  * interruptibility
  * fairness

### When is ReentrantLock better than Synchronized
* You need fallback logic. E.g.: Try booking seat → if locked → pick another
* You want to avoid deadlocks.
  * Use: ```tryLock(timeout)```
  * Instead of: two threads waiting forever holding partial locks
* You want responsiveness - Don’t let threads block forever
* Complex coordination
  * Multiple locks
  * Conditional flows
  * Retry strategies
* Example: Seat booking system
```
// With synchronized: thread blocks indefinitely on contention
// With ReentrantLock: fail fast and recover

public boolean bookSeat(String seatId) {
    ReentrantLock seatLock = locks.get(seatId);
    if (seatLock.tryLock(500, TimeUnit.MILLISECONDS)) {
        try {
            if (!seat.isBooked()) {
                seat.book();
                return true;
            }
        } finally {
            seatLock.unlock();
        }
    }
    return false; // timed out - caller can retry or pick another seat
}  
```

---

# ReentrantReadWriteLock
```
private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
private final Lock readLock  = rwLock.readLock();
private final Lock writeLock = rwLock.writeLock();

// Multiple readers in parallel
public String get(String key) {
    readLock.lock();
    try {
        return cache.get(key);
    } finally {
        readLock.unlock();
    }
}

// Exclusive writer
public void put(String key, String value) {
    writeLock.lock();
    try {
        cache.put(key, value);
    } finally {
        writeLock.unlock();
    }
}
```

### What is does?
* Two locks:
  * Read lock: shared - multiple threads can hold it simultaneously
  * Write lock: exclusive - blocks all readers and other writers
* Working: Multiple readers run in parallel, writers serialize

### When to use?
* Read-heavy systems, light-write systems. E.g.: Product catalog, Configuration cache

### Pros
* Much better throughput in read-heavy systems

### Cons
* Write starvation - continuous reads can indefinitely starve a waiting writer
* If reads ≈ writes → user ReentrantLock (simpler, less overhead)

### Comparison with ReentrantLock
You’re basically choosing between:
* ReentrantLock:
  * One lock for everything
  * One thread at a time. Everyone else waits.
* ReentrantReadWriteLock
  * Two locks (one reader, one writer), so coordination required between them
  * Multiple readers allowed. Writers get exclusivity.

Scenario: In-memory cache
With ReentrantLock
```
lock.lock();
try {
    return map.get(key);
} finally {
    lock.unlock();
}
```
Even if 100 threads are just reading, only one thread runs at a time. Congrats, you just serialized your system.

With ReentrantReadWriteLock
```
readLock.lock();
try {
    return map.get(key);
} finally {
    readLock.unlock();
}
```
Now, 100 readers → all run in parallel. 1 writer → exclusive

So, ReentrantReadWrite wins when Reads are MUCH more frequent than writes.

But, in the following conditions ReentrantReadWriteLock fails:
* Case 1: Balanced read-write (50/50)
  * Now, Readers keep getting blocked by writers. So, writers keep waiting for readers
  * Extra overhead + no gain
* Case 2: Write-heavy system
  * Example: Payment updates, Order processing
  * Here: Writes dominate -> always exclusive anyway -> ReentrantLock is simpler and faster


### When is this better than ReentrantLock
* Read-heavy workloads (Reads >> Writes - like 90/10 split)

---

# How to choose?
* Is it read-heavy (90%+ reads)?
  * ReentrantReadWriteLock
* Do you need tryLock/timeout/fairness?
  * ReentrantLock
* Otherwise
  * Eg: Simple shared critical resource, or, moderate contention
    * Synchronized

---

# Common Pitfalls
* ❌ Ignoring contention pattern
  * Choosing lock without:
    * read/write ratio
    * thread count
    * critical section size

* ❌ Using ReentrantLock without reason
  * If you're not using tryLock, timeout, or Condition, you just wrote verbose synchronized.
* ❌ Using ReadWriteLock everywhere
  * You’re not Google. Your system probably isn’t read-heavy enough. Validate the read/write ratio first.
* ❌ Forgetting unlock() in ReentrantLock
  * Always wrap in try/finally. Missing unlock = silent deadlock that only surfaces under load.
* ❌ Using a single lock for independent resources
```
// ❌ Serializes all seat bookings through one lock
synchronized (this) { bookSeat(seatId); }

// ✅ Per-resource lock - only contend on the same seat
ConcurrentHashMap<String, ReentrantLock> seatLocks;
seatLocks.computeIfAbsent(seatId, k -> new ReentrantLock()).lock();
```

---

# Interview Talking Points
* Always start simple - synchronized first, then evolve based on requirements
* Lock granularity matters - one global lock vs. per-resource locks changes throughput entirely
* Contention analysis - read/write ratio, number of threads, critical section size all feed the lock choice
* Compound operations need locks - AtomicInteger alone doesn't protect check-then-act sequences

---

# Examples
1. Cache system
   * 95% reads
   * Occasional updates
   * ReadWriteLock

2. Booking system
   * Multiple writes (seat booking)
   * Conflicts common
   * ReentrantLock
   * RWLock gives no benefit here

3. Payment ledger
   * Every operation modifies state
   * ReentrantLock

4. Config service
   * Rarely updated
   * Heavily read
   * ReadWriteLock

