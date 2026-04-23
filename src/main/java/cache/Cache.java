package cache;

import cache.evictionPolicy.EvictionPolicy;
import cache.exceptions.NotFoundException;
import cache.exceptions.StorageFullException;
import cache.storage.Storage;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Every operation mutates LRU order, so no read concurrency is achievable.
 * ReentrantLock is correct here - ReadWriteLock adds complexity for zero benefit.
 */
public class Cache<K, V> {
    private final Storage<K, V> storage;
    private final EvictionPolicy<K> evictionPolicy;
    private final ReentrantLock lock = new ReentrantLock();

    public Cache(Storage<K, V> storage, EvictionPolicy<K> evictionPolicy) {
        this.storage = storage;
        this.evictionPolicy = evictionPolicy;
    }

    // NOTE: get is a WRITE operation - it updates LRU order in eviction policy.
    public V get(K key) {
        lock.lock();
        try {
            V value = storage.get(key);
            evictionPolicy.keyAccessed(key);
            return value;
        } catch (NotFoundException e) {
            return null;
        } finally {
            lock.unlock();
        }
    }

    public void put(K key, V value) {
        lock.lock();
        try {
            try {
                storage.put(key, value);
                evictionPolicy.keyAccessed(key);
            } catch (StorageFullException e) {
                evict();
                storage.put(key, value);
                evictionPolicy.keyAccessed(key);
            }
        } finally {
            lock.unlock();
        }
    }

    public void remove(K key) {
        lock.lock();
        try {
            storage.remove(key);
            evictionPolicy.keyRemoved(key);
        } catch (NotFoundException ignored) {
            // Idempotent remove - intentional, not an oversight.
        } finally {
            lock.unlock();
        }
    }

    // evictKey() atomically removes from policy state.
    // Do NOT call keyRemoved() after - it would be a double-remove.
    private void evict() {
        K evictedKey = evictionPolicy.evictKey();
        if (evictedKey != null) {
            storage.remove(evictedKey);
        }
    }
}