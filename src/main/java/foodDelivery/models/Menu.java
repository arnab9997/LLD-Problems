package foodDelivery.models;

import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Menu {
    private final Map<String, MenuItem> items = new HashMap<>();

    public void addItem(MenuItem item) {
        items.put(item.getId(), item);
    }

    public MenuItem getItem(String id) {
        return items.get(id);
    }

    public Collection<MenuItem> getItems() {
        return items.values();
    }
}
