# ConcurrentHashMap
```
ConcurrentHashMap<String, UserSession> sessions = new ConcurrentHashMap<>();
sessions.put(userId, session);
sessions.get(userId);          // non-blocking read
sessions.remove(userId);
```

### What is does
* Internally partitioned into segments (Java 8+: per-bucket CAS + synchronized on head node only)
* Reads are mostly non-blocking; writes lock only the affected bucket, not the whole map
* Far better throughput than HashMap + synchronized under contention

### Why not HashMap + synchronized?
A single lock serializes every read and write across all threads - you've just made a concurrent map single-threaded.

``` Critical: Compound operations are NOT atomic
// ❌ TOCTOU race - another thread can insert between get() and put()
if (!map.containsKey(key)) {
    map.put(key, new Value());
}

// ✅ Atomic - use built-in compound methods
map.putIfAbsent(key, new Value());

// ✅ Even better for expensive initialization
map.computeIfAbsent(key, k -> new Value());
```

``` computeIfAbsent — Imp LLD pattern to implement per-resource locking without race condition:
// Parking lot, booking system, ATM - per-resource lock map
private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

public void processResource(String resourceId) {
    // Atomically gets existing lock or creates a new one - no TOCTOU
    ReentrantLock lock = locks.computeIfAbsent(resourceId, k -> new ReentrantLock());
    lock.lock();
    try {
        // safe to work on this specific resource
    } finally {
        lock.unlock();
    }
}
```

``` Other useful atomic operations
map.putIfAbsent(key, value);           // insert only if absent
map.replace(key, oldVal, newVal);      // conditional update
map.remove(key, value);                // conditional remove
map.merge(key, 1, Integer::sum);       // atomic read-modify-write (e.g., frequency count)
map.compute(key, (k, v) -> v == null ? 1 : v + 1);  // same as merge but more general
```

### When to use
* Shared mutable key-value state with concurrent reads and writes
* Session stores, cache maps, lock registries, frequency counters

### Cons
* Compound logic spanning multiple keys is still not atomic (need external locking for that)
* Iteration reflects state at the time of iteration - not a point-in-time snapshot

---

# CopyOnWriteArrayList
```
CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();

listeners.add(listener);      // write: copies entire array
listeners.remove(listener);   // write: copies entire array

for (EventListener l : listeners) {  // reads snapshot — never throws ConcurrentModificationException
    l.onEvent(event);
}
```

### What it does
* Every write creates a full copy of the underlying array; readers always see an immutable snapshot
* Reads require zero locks - they read from the stable snapshot at the time iteration started

### When to use
* Reads vastly outnumber writes (think 1000:1 or more)
* You need safe iteration without locking: event listeners, config watchers, plugin registries

### Pros
* Completely lock-free reads
* Iteration never throws ConcurrentModificationException
* Snapshot isolation - iterating thread sees a consistent view even if writes happen during iteration

### Cons
* Writes are expensive - O(n) copy every time
* Memory overhead - two copies of the array exist transiently during writes
* Not suitable if writes are even moderately frequent

### LLD use case
```
// Notification system - listeners registered rarely, events fired constantly
class NotificationService {
    private final CopyOnWriteArrayList<NotificationListener> listeners = new CopyOnWriteArrayList<>();

    public void register(NotificationListener l) { listeners.add(l); }

    public void notify(Event event) {
        for (NotificationListener l : listeners) {  // safe, no lock needed
            l.onEvent(event);
        }
    }
}
```

---

# BlockingQueue
```
BlockingQueue<Task> queue = new LinkedBlockingQueue<>(100); // bounded

// Producer - blocks if queue is full (backpressure)
queue.put(task);

// Consumer - blocks until item is available
Task task = queue.take();

// Non-blocking variants (for timeouts / polling loops)
boolean added = queue.offer(task, 500, TimeUnit.MILLISECONDS);  // returns false if full
Task t = queue.poll(500, TimeUnit.MILLISECONDS);                 // returns null if empty
```

### When to use
* Producer-consumer with backpressure (bound the queue to prevent runaway memory)
* Task scheduling, logging pipelines, thread pool work queues

### Types
* ArrayBlockingQueue -> SynchronousQueue
* ArrayBlockingQueue -> Unbounded by default - memory risk
* PriorityBlockingQueue -> Ordered by priority, unbounded
* SynchronousQueue -> Zero capacity - direct handoff between threads

### Pros
* Built-in blocking semantics - no manual wait()/notify() needed
* Backpressure: bounded queue naturally slows producers when consumers are behind
* Thread-safe out of the box

### Cons
* Blocking threads consume resources - don't use unbounded queues if producers can outrun consumers
* put()/take() can become a bottleneck at very high throughput (prefer ConcurrentLinkedQueue there)

### LLD Use case
``` Logger system
class AsyncLogger {
    private final BlockingQueue<LogEntry> buffer = new ArrayBlockingQueue<>(1000);

    // Called by many threads
    public void log(LogEntry entry) {
        if (!buffer.offer(entry)) {
            // Queue full — drop or fallback to sync logging
        }
    }

    // Single background thread drains and writes
    private void startWriter() {
        new Thread(() -> {
            while (true) {
                LogEntry entry = buffer.take(); // blocks until available
                writer.write(entry);
            }
        }).start();
    }
}
```

---

# ConcurrentLinkedQueue
```
ConcurrentLinkedQueue<Event> queue = new ConcurrentLinkedQueue<>();

queue.offer(event);    // non-blocking add (always succeeds)
Event e = queue.poll(); // non-blocking remove — returns null if empty
queue.peek();          // look at head without removing
```

### What it does
* Lock-free, CAS-based linked queue
* offer() and poll() never block - they either succeed immediately or return null/false

### When to use
* High-throughput, low-latency event buffers where you cannot afford blocking
* Metrics collection, request tracing, async event pipelines
* When you have your own consumer loop that handles empty gracefully

### Pros
* Lock-free (CAS) → extremely high throughput
* No thread contention on empty/full

### Cons
* No blocking - you must poll in a loop or use a separate signal mechanism
* No backpressure - unbounded, so memory can grow without limit
* size() is O(n) - don't call it in hot paths

### When to choose over BlockingQueue
* When latency matters more than coordination
* When you can tolerate eventual processing
* Rule: If your consumer should wait for work, use BlockingQueue. If your consumer polls in a loop or is event-driven, use ConcurrentLinkedQueue.

---

# Atomic classes (AtomicInteger, AtomicLong, AtomicBoolean etc.)
```
AtomicInteger counter = new AtomicInteger(0);

counter.incrementAndGet();              // atomic ++, returns new value
counter.getAndIncrement();              // atomic ++, returns old value
counter.addAndGet(5);                   // atomic +=
counter.compareAndSet(expected, update); // CAS — only updates if current == expected
```

### What it does
* Uses CPU-level CAS instructions - no OS lock, no context switch
* Safe for single-variable operations only

### When to use
* Shared counters (request count, active connections, ID generators)
* Single-value flags (AtomicBoolean for shutdown signals)
* Anywhere you'd otherwise put synchronized around a single variable

``` CAS doesn't protect compound logic
// ❌ Race condition — two operations, not atomic
if (counter.get() < limit) {
    counter.incrementAndGet();  // another thread can breach limit between these two
}

// ✅ Use compareAndSet loop for conditional increment
int current;
do {
    current = counter.get();
    if (current >= limit) return false; // limit already reached
} while (!counter.compareAndSet(current, current + 1));
return true;

// ✅ Or just use a lock if logic is complex - don't force CAS on multi-step logic```
```
### Pros
* Lock-free
* Very fast under low contention

### Cons
* Limited to simple operations
* Complex logic becomes unreadable garbage
Bad idea:
```if (counter.get() < 10) counter.incrementAndGet(); // race condition```

---

# LLD Design use cases

| LLD Problem | Concurrent Structure Used | Why |
|---|---|---|
| Parking Lot | `ConcurrentHashMap<slotId, Lock>` + `computeIfAbsent` | Per-slot locking without TOCTOU |
| Booking System | `ConcurrentHashMap<resourceId, ReentrantLock>` | Per-resource lock registry |
| Logging Framework | `BlockingQueue<LogEntry>` | Async producer-consumer with backpressure |
| Pub/Sub / Event Bus | `CopyOnWriteArrayList<Listener>` | Safe iteration during event dispatch |
| Rate Limiter | `ConcurrentHashMap<userId, AtomicInteger>` | Per-user counters without global lock |
| Metrics Collection | `ConcurrentLinkedQueue<Metric>` | High-throughput, non-blocking |
 
---

## Common Pitfalls

* ❌ Assuming `ConcurrentHashMap` makes compound operations atomic**
  * `get()` + `put()` is two operations. Use `computeIfAbsent()`, `putIfAbsent()`, or `compute()`.
* ❌ Using `CopyOnWriteArrayList` when writes are not rare
  * The copy-on-write cost scales with list size. Profile first — it's designed for extreme read dominance.
* ❌ Unbounded `LinkedBlockingQueue` in a producer-consumer
  * Producers can outrun consumers silently; the queue grows until OOM. Always bound it and handle `offer()` failures explicitly.
* ❌ Calling `size()` on `ConcurrentLinkedQueue` in a hot path
  * It's O(n) — traverses the whole queue. Cache the size separately with an `AtomicInteger` if you need it.
* ❌ Using `AtomicInteger` for multi-step logic
  * CAS protects one variable, one operation. Use a lock for check-then-act or multi-variable consistency.
* ❌ Using `HashMap` + `synchronized(this)` under contention
  * A single lock on the entire map serializes all threads. Use `ConcurrentHashMap` instead.
 