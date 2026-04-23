package foodDelivery.models;

import lombok.Getter;

@Getter
public class MenuItem {
    private final String id;
    private final String name;
    private final double price;
    private final boolean available;

    public MenuItem(String id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.available = true;
    }
}
