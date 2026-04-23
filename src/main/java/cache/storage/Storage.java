package cache.storage;

import cache.exceptions.NotFoundException;
import cache.exceptions.StorageFullException;

// put() inserts OR updates.
// StorageFullException thrown only when key is new and capacity is exhausted.
public interface Storage<K, V> {
    void put(K key, V value) throws StorageFullException;
    V get(K key) throws NotFoundException;
    void remove(K key) throws NotFoundException;
}
