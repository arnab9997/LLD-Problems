package keyValueStore;

import java.util.Map;
import java.util.Set;

public interface ISecondaryIndex {
    void add(String key, Map<String, Object> attributes);
    void remove(String key, Map<String, Object> attributes);
    Set<String> search(String attributeKey, Object attributeValue);
}
