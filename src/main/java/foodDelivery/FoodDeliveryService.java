package foodDelivery;

import foodDelivery.enums.OrderStatus;
import foodDelivery.exception.EntityNotFoundException;
import foodDelivery.models.*;
import foodDelivery.order.Order;
import foodDelivery.order.OrderItem;
import foodDelivery.strategy.assignment.DeliveryAssignmentStrategy;
import foodDelivery.strategy.search.RestaurantSearchStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FoodDeliveryService {

    private static volatile FoodDeliveryService INSTANCE;

    private final Map<String, Customer> customers = new HashMap<>();
    private final Map<String, Restaurant> restaurants = new HashMap<>();
    private final Map<String, DeliveryAgent> deliveryAgents = new HashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final DeliveryAssignmentStrategy assignmentStrategy;

    private FoodDeliveryService(DeliveryAssignmentStrategy assignmentStrategy) {
        this.assignmentStrategy = assignmentStrategy;
    }

    public static FoodDeliveryService getInstance(DeliveryAssignmentStrategy strategy) {
        if (INSTANCE == null) {
            synchronized (FoodDeliveryService.class) {
                if (INSTANCE == null) INSTANCE = new FoodDeliveryService(strategy);
            }
        }
        return INSTANCE;
    }

    // ── Registration ──────────────────────────────────────────────────────────

    public Customer registerCustomer(String name, String phone, Address address) {
        Customer c = new Customer(name, phone, address);
        customers.put(c.getId(), c);
        return c;
    }

    public Restaurant registerRestaurant(String name, Address address) {
        Restaurant r = new Restaurant(name, address);
        restaurants.put(r.getId(), r);
        return r;
    }

    public void registerDeliveryAgent(String name, String phone, Address location) {
        DeliveryAgent a = new DeliveryAgent(name, phone, location);
        deliveryAgents.put(a.getId(), a);
    }

    // ── Order lifecycle ───────────────────────────────────────────────────────

    public Order placeOrder(String customerId, String restaurantId, List<OrderItem> items) {
        Customer customer     = getOrThrow(customers, customerId, "Customer");
        Restaurant restaurant = getOrThrow(restaurants, restaurantId, "Restaurant");

        Order order = new Order(customer, restaurant, items);
        orders.put(order.getId(), order);
        customer.addOrderToHistory(order);

        System.out.printf("[Order] %s placed by '%s' at '%s' \n", order.getId(), customer.getName(), restaurant.getName());

        order.transitionTo(OrderStatus.CONFIRMED);
        return order;
    }

    public void startPreparingOrder(String orderId) {
        getOrThrow(orders, orderId, "Order").transitionTo(OrderStatus.PREPARING);
    }

    public void markOrderReadyForPickup(String orderId) {
        Order order = getOrThrow(orders, orderId, "Order");
        order.transitionTo(OrderStatus.READY_FOR_PICKUP);
        assignDelivery(order);
    }

    public void markOrderDelivered(String orderId) {
        Order order = getOrThrow(orders, orderId, "Order");
        order.transitionTo(OrderStatus.DELIVERED);
        if (order.getDeliveryAgent() != null) {
            order.getDeliveryAgent().releaseFromDelivery();
        }
    }

    public void cancelOrder(String orderId) {
        getOrThrow(orders, orderId, "Order").transitionTo(OrderStatus.CANCELLED);
    }

    // ── Search ────────────────────────────────────────────────────────────────

    public List<Restaurant> searchRestaurants(List<RestaurantSearchStrategy> strategies) {
        List<Restaurant> results = new ArrayList<>(restaurants.values());
        for (RestaurantSearchStrategy strategy : strategies) {
            results = strategy.filter(results);
        }
        return results;
    }

    public Menu getRestaurantMenu(String restaurantId) {
        return getOrThrow(restaurants, restaurantId, "Restaurant").getMenu();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void assignDelivery(Order order) {
        assignmentStrategy
                .findAgent(order, new ArrayList<>(deliveryAgents.values()))
                .ifPresentOrElse(
                        agent -> {
                            System.out.printf("[Assignment] Agent '%s' -> Order %s \n", agent.getName(), order.getId());
                            order.assignDeliveryAgent(agent);
                            order.transitionTo(OrderStatus.OUT_FOR_DELIVERY);
                        },
                        () -> { throw new EntityNotFoundException("No available agent for order: " + order.getId());
                        }
                );
    }

    private <T> T getOrThrow(Map<String, T> map, String id, String entityName) {
        T value = map.get(id);
        if (value == null) throw new EntityNotFoundException(entityName + " not found: " + id);
        return value;
    }
}