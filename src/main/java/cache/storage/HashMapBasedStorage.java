package cache.storage;

import cache.exceptions.NotFoundException;
import cache.exceptions.StorageFullException;

import java.util.HashMap;
import java.util.Map;

public class HashMapBasedStorage<K, V> implements Storage<K, V> {
    private final Map<K, V> store = new HashMap<>();
    private final int capacity;

    public HashMapBasedStorage(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        this.capacity = capacity;
    }

    @Override
    public void put(K key, V value) throws StorageFullException {
        if (!store.containsKey(key) && store.size() >= capacity) {
            throw new StorageFullException("Storage at capacity: " + capacity);
        }
        store.put(key, value);
    }

    @Override
    public V get(K key) throws NotFoundException {
        if (!store.containsKey(key)) throw new NotFoundException("Key not found: " + key);
        return store.get(key);
    }

    @Override
    public void remove(K key) throws NotFoundException {
        if (!store.containsKey(key)) throw new NotFoundException("Key not found: " + key);
        store.remove(key);
    }
}

