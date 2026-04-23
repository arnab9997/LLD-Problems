package foodDelivery.models;

import foodDelivery.order.Order;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Customer extends User {
    private final Address address;
    private final List<Order> orderHistory;

    public Customer(String name, String phone, Address address) {
        super(name, phone);
        this.address = address;
        this.orderHistory = new ArrayList<>();
    }

    @Override
    public void onOrderUpdated(Order order) {
        System.out.printf("[Customer: %s] Order %s -> %s \n", getName(), order.getId(), order.getStatus());
    }

    public void addOrderToHistory(Order order) {
        this.orderHistory.add(order);
    }
}
