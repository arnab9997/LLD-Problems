package keyValueStore;

import java.util.List;
import java.util.Map;

public interface KeyValueStore {
    void put (String key, Map<String, Object> attributes);
    Map<String, Object> get(String key);
    void delete(String key);
    List<String> search(String attributeKey, Object attributeValue);
}
