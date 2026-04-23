package cache;

import cache.evictionPolicy.LRUEvictionPolicy;
import cache.storage.HashMapBasedStorage;

public class CacheDemo {
    public static void main(String[] args) {
        Cache<Integer, String> cache = new Cache<>(new HashMapBasedStorage<>(2), new LRUEvictionPolicy<>());

        cache.put(1, "A");
        cache.put(2, "B");
        cache.get(1);        // promotes 1 to MRU; 2 becomes LRU

        cache.put(3, "C");   // evicts 2, inserts 3

        System.out.println(cache.get(2)); // null  — evicted
        System.out.println(cache.get(1)); // A
        System.out.println(cache.get(3)); // C

        cache.put(1, "A2");  // update — no eviction
        System.out.println(cache.get(1)); // A2

        cache.remove(3);
        System.out.println(cache.get(3)); // null
        cache.remove(3);     // idempotent — no exception
    }
}