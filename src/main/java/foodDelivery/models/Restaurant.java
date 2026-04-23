package foodDelivery.models;

import foodDelivery.observer.OrderObserver;
import foodDelivery.order.Order;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Restaurant implements OrderObserver {
    private final String id;
    private final String name;
    private final Address address;
    private final Menu menu;

    public Restaurant(String name, Address address) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.address = address;
        this.menu = new Menu();
    }

    public void addToMenu(MenuItem item) {
        menu.addItem(item);
    }

    @Override
    public void onOrderUpdated(Order order) {
        System.out.printf("[Restaurant: %s] Order %s -> %s \n", name, order.getId(), order.getStatus());
    }
}
