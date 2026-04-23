package keyValueStore;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SecondaryIndex implements ISecondaryIndex {

    // attributeKey -> (attributeValue -> Set of keys)
    private final Map<String, Map<Object, Set<String>>> index = new HashMap<>();

    @Override
    public void add(String key, Map<String, Object> attributes) {
        for (var entry : attributes.entrySet()) {
            index.computeIfAbsent(entry.getKey(), k -> new HashMap<>())
                    .computeIfAbsent(entry.getValue(), v -> new HashSet<>())
                    .add(key);
        }
    }

    @Override
    public void remove(String key, Map<String, Object> attributes) {
        for (var entry : attributes.entrySet()) {
            Map<Object, Set<String>> valueMap = index.get(entry.getKey());
            if (valueMap == null) {
                continue;
            }

            Set<String> keys = valueMap.get(entry.getValue());
            if (keys != null) {
                keys.remove(key);
            }
        }
    }

    @Override
    public Set<String> search(String attributeKey, Object attributeValue) {
        return index.getOrDefault(attributeKey, Collections.emptyMap())
                .getOrDefault(attributeValue, Collections.emptySet());
    }
}
