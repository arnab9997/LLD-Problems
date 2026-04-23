package cache.evictionPolicy;

import cache.DoublyLinkedList;
import cache.DoublyLinkedListNode;

import java.util.HashMap;
import java.util.Map;

// Not independently thread-safe - relies on Cache's ReentrantLock.
public class LRUEvictionPolicy<K> implements EvictionPolicy<K> {
    private final DoublyLinkedList<K> dll = new DoublyLinkedList<>();
    private final Map<K, DoublyLinkedListNode<K>> nodeMap = new HashMap<>();

    @Override
    public void keyAccessed(K key) {
        if (nodeMap.containsKey(key)) {
            dll.moveToLast(nodeMap.get(key));
        } else {
            DoublyLinkedListNode<K> node = dll.addLast(key);
            nodeMap.put(key, node);
        }
    }

    @Override
    public void keyRemoved(K key) {
        DoublyLinkedListNode<K> node = nodeMap.remove(key);
        if (node != null) dll.remove(node);
    }

    // Atomically selects AND removes LRU key from policy state.
    // Cache.evict() must NOT call keyRemoved() after this.
    @Override
    public K evictKey() {
        DoublyLinkedListNode<K> lruNode = dll.removeFirst();
        if (lruNode == null) return null;
        nodeMap.remove(lruNode.getKey());
        return lruNode.getKey();
    }
}