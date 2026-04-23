package foodDelivery.order;

import foodDelivery.enums.OrderStatus;
import foodDelivery.models.Customer;
import foodDelivery.models.DeliveryAgent;
import foodDelivery.models.Restaurant;
import foodDelivery.observer.OrderObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order {
    private final String id;
    private final Customer customer;
    private final Restaurant restaurant;
    private final List<OrderItem> items;

    // Guarded by intrinsic lock (this)
    private OrderStatus status;
    private DeliveryAgent deliveryAgent;

    private final List<OrderObserver> observers = new ArrayList<>();

    public Order(Customer customer, Restaurant restaurant, List<OrderItem> items) {
        this.id = UUID.randomUUID().toString();
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = List.copyOf(items);
        this.status = OrderStatus.PENDING;
        observers.add(customer);
        observers.add(restaurant);
    }

    /**
     * Single transition method — validation delegated to the enum.
     * OrderStatus owns the transition rules; Order owns thread-safety and notification.
     */
    public synchronized void transitionTo(OrderStatus newStatus) {
        status.validateTransition(newStatus);
        this.status = newStatus;
        observers.forEach(o -> o.onOrderUpdated(this));
    }

    public synchronized void assignDeliveryAgent(DeliveryAgent agent) {
        this.deliveryAgent = agent;
        observers.add(agent);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String getId()             { return id; }
    public Customer getCustomer()     { return customer; }
    public Restaurant getRestaurant() { return restaurant; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }

    public synchronized OrderStatus getStatus()          { return status; }
    public synchronized DeliveryAgent getDeliveryAgent() { return deliveryAgent; }
}