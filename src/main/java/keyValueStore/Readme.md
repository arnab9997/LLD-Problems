## Functional Requirements
* Store key -> multi-attribute value
  * eg: ```"user1" -> { "name": "arnab", "age": 25 }```
* put(key, attributes)
  * Insert or update a key
  * Overwrites existing value
  * Update secondary index accordingly
* get(key)
  * Fetch value for a key
  * Throw exception if key doesn't exist
* delete(key)
  * Remove key from store
  * Clean up corresponding index entries
* search(attributeKey, attributeValue)
  * Exact match on a single attribute
  * Returns sorted list of matching keys
---

## Non-functional requirements
* Strong consistency between primary store and secondary index
* Thread safe operations
* Optimized for read-heavy workloads:
  * Lock-free get
  * Serialized writes and searches
* Time complexity:
  * put/delete: O(n) over attributes
  * get: O(1)
  * search: O(k*logk) (sorting result)

---

## Core entities
* keyValueStore
* SecondaryIndex

---

## Enums
N/A

---

## State Models
N/A

---

## Design Patterns
* Strategy Design Pattern -> Allowing plugging different kv stores & indexing strategies
* Defensive copying:
  * Input map copied during put
  * Output map wrapped using Collections.unmodifiableMap

---

## Concurrency
* ConcurrentHashMap for primary store
* synchronized(this) for cross-structure consistency
* Why this is needed
  * Store and index must be updated atomically
  * ConcurrentHashMap alone does NOT guarantee atomicity across:
    * store.put/remove
    * index.add/remove
* Current behavior
  * get -> lock-free
  * put, delete, search -> fully serialized
* Trade-offs
  * Pros:
    * Simple and correct
    * No race conditions between store and index
  * Cons:
    * Global lock = scalability bottleneck
    * search unnecessarily blocked by writes
    * Poor performance under high write contention
* Upgrade path
  * Replace with ReentrantReadWriteLock:
    * Multiple concurrent reads (get, search)
    * Exclusive writes (put, delete)
---

## API Design
* void put(String key, Map<String, Object> attributes);
* Map<String, Object> get(String key);
* void delete(String key);
* List<String> search(String attributeKey, Object attributeValue);

---

## DB Persistence
N/A

---

## Notes