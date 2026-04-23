package cache.evictionPolicy;

/**
 * keyAccessed : call on every get() and put()
 * keyRemoved  : call on explicit remove() ONLY — not after evictKey()
 * evictKey    : atomically selects AND removes from policy state; caller must not call keyRemoved() afterward
 */
public interface EvictionPolicy<K> {
    void keyAccessed(K key);
    void keyRemoved(K key);
    K evictKey();
}