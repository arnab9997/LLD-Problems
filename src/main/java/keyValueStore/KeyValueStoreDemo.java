package keyValueStore;

import java.util.HashMap;
import java.util.Map;

public class KeyValueStoreDemo {
    public static void main(String[] args) {
        KeyValueStore kvStore = new InMemoryKeyValueStore();

        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", "arnab");
        user1.put("age", 25);

        kvStore.put("user1", user1);

        System.out.println(kvStore.get("user1"));
        System.out.println(kvStore.search("age", 25));
        kvStore.delete("user1");
    }
}
