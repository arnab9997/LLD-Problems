package keyValueStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Assumptions:
 * - Keys are unique strings; null keys are rejected
 * - search() is exact-match only on a single attribute
 * - Strong consistency - search results always reflect the latest committed put/delete
 *
 *
 * <p><b>Why locking strategy alongside ConcurrentHashMap:</b><br>
 * ConcurrentHashMap guarantees atomicity per operation, not across operations.
 * This store maintains two separate maps (store + index) that must stay
 * consistent with each other. Without coordination, a thread can observe
 * a torn state - e.g., a key present in the store but missing from the index.
 * {@code synchronized} guards this cross-map invariant, not the map operations themselves.
 *
 * <p><b>Locking strategy and trade-off:</b><br>
 * {@code synchronized(this)} serializes writes ({@code put}, {@code delete})
 * and searches, while {@code get} remains lock-free via ConcurrentHashMap.
 * Write throughput is serialized; read throughput scales freely.
 *
 * <p>If profiling shows reads heavily dominating writes, the upgrade path
 * is {@link ReentrantReadWriteLock} - allowing concurrent reads while still serializing writes.
 */
public class InMemoryKeyValueStore implements KeyValueStore {

    private final Map<String, Map<String, Object>> store;
    private final ISecondaryIndex index;

    // ReadWriteLock ensures store + index updates are atomic.
    // Multiple concurrent reads are allowed; writes are exclusive.
    // private final ReadWriteLock lock;

    public InMemoryKeyValueStore() {
        this.store = new ConcurrentHashMap<>();
        this.index = new SecondaryIndex();
        // this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public void put(String key, Map<String, Object> attributes) {
        validatePutInputs(key, attributes);
        Map<String, Object> newAttributes = new HashMap<>(attributes);

        synchronized (this) {
            Map<String, Object> oldAttributes = store.put(key, newAttributes);
            if (oldAttributes != null) {
                index.remove(key, oldAttributes);
            }
            index.add(key, newAttributes);
        }
    }

    @Override
    public Map<String, Object> get(String key) {
        // Lock-free - ConcurrentHashMap.get() is safe standalone
        Map<String, Object> result = store.get(key);
        if (result == null) {
            throw new StoreException("Key not found: " + key);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public void delete(String key) {
        synchronized (this) {
            Map<String, Object> removed = store.remove(key);
            if (removed == null) {
                throw new StoreException("Key not found: " + key);
            }
            index.remove(key, removed);
        }
    }

    @Override
    public List<String> search(String attributeKey, Object attributeValue) {
        if (attributeKey == null || attributeValue == null) {
            throw new StoreException("Search attributes cannot be null");
        }

        synchronized (this) {
            Set<String> result = index.search(attributeKey, attributeValue);
            List<String> sorted = new ArrayList<>(result);
            Collections.sort(sorted);
            return sorted;
        }
    }

    private void validatePutInputs(String key, Map<String, Object> attributes) {
        if (key == null || attributes == null) {
            throw new StoreException("Key and attributes cannot be null");
        }
        for (var entry : attributes.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new StoreException("Attribute keys and values cannot be null");
            }
        }
    }
}
