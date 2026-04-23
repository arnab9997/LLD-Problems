## Functional Requirements
* Support basic cache operations:
  * get(key) → returns value or null if not present
  * put(key, value) → inserts or updates value
  * remove(key) → removes key if present (idempotent)
* Enforce fixed capacity
* Automatically evict least recently used (LRU) item on capacity breach
* Update recency on:
  * get
  * put (both insert and update)
* Eviction must:
  * Remove key from both storage and eviction policy
  * Be atomic from policy perspective

---

## Non-functional requirements
* Time Complexity
  * get: O(1)
  * put: O(1)
  * remove: O(1)
* Thread Safety
  * Fully thread-safe
* Consistency
  * Strong consistency between storage and eviction policy
* Scalability
  * Single-node, in-memory (no distribution)
* Extensibility
  * Pluggable eviction policies
  * Pluggable storage implementations

---

## Core entities
* DoublyLinkedListNode<K>
* DoublyLinkedList<K>
* EvictionPolicy<K>
* Storage<K, V>
* Cache<K, V>
---

## Enums
N/A

---

## State Models
N/A

---

## Design Patterns
* Strategy Design Pattern - For Eviction policy to allow switching between LRU, LFU, FIFO etc.r)

---

## Concurrency
* Uses ReentrantLock for thread safety
* Entire cache is guarded by a single lock

Why not ReadWriteLock?
  get() modifies access order → it's a write operation So:
  - No real read-only operations
  - ReadWriteLock adds complexity without benefit

---

## API Design
* V get(K key) - Return value if present, else null
* void put(K key, V value) - Insert/Update key. On capacity breach, evict LRU and insert
* void remove(K key) - Remove key if present, else no-op

---

## DB Persistence
N/A

---

## Notes

### Why `DoublyLinkedListNode<K>` and not `<K, V>`?

A generic (`<K>`, `<V>`, `<K, V>`) is a type placeholder filled in by the caller.
```java
Cache<Integer, String> cache = new Cache<>(...); // Every K in Cache becomes Integer, every V becomes String
```

Why the node only needs `<K>`

The DLL lives inside `LRUEvictionPolicy`, which has one job: **track key order**.  
It does not care what any key maps to. That is `HashMapBasedStorage`'s job.

```
LRUEvictionPolicy   ->  owns key ordering   (DLL of keys)
HashMapBasedStorage ->  owns key-value data  (HashMap)
```

So the node only stores a key:
```java
class DoublyLinkedListNode<K> {
    private final K key;   // just the key — no value needed
    DoublyLinkedListNode<K> prev;
    DoublyLinkedListNode<K> next;
}
```

What goes wrong with `<K, V>`?

If the node stored the value too, the same value would exist in two places:

| Location | State after `put(1, "B")` |
|---|---|
| `HashMapBasedStorage` | `1 → "B"` ✅ |
| `DoublyLinkedListNode` | `1 → "A"` ❌ (stale if you forgot to update) |

Two sources of truth = guaranteed consistency bugs.

The rule:
* Only store data in the component responsible for it.  
* `EvictionPolicy` is responsible for order. `Storage` is responsible for values.  
* The node reflects that boundary — `<K>` only.