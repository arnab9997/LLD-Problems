package foodDelivery;

import foodDelivery.exception.IllegalStateTransitionException;
import foodDelivery.models.Address;
import foodDelivery.models.Customer;
import foodDelivery.models.MenuItem;
import foodDelivery.models.Restaurant;
import foodDelivery.order.Order;
import foodDelivery.order.OrderItem;
import foodDelivery.strategy.assignment.NearestAvailableAgentStrategy;
import foodDelivery.strategy.search.SearchByCityStrategy;
import foodDelivery.strategy.search.SearchByMenuKeywordStrategy;
import foodDelivery.strategy.search.SearchByProximityStrategy;

import java.util.List;

public class FoodDeliveryServiceDemo {
    public static void main(String[] args) {
        FoodDeliveryService service =
                FoodDeliveryService.getInstance(new NearestAvailableAgentStrategy());

        // ── Setup ─────────────────────────────────────────────────────────────
        Address aliceAddr = new Address("123 Maple St", "Springfield", "12345", 40.7128, -74.0060);
        Address pizzaAddr = new Address("456 Oak Ave", "Springfield", "12345", 40.7138, -74.0070);
        Address burgerAddr = new Address("789 Pine Ln", "Springfield", "12345", 40.7108, -74.0050);
        Address tacoAddr = new Address("101 Elm Ct", "Shelbyville", "54321", 41.7528, -75.0160);
        Address bobAddr = new Address("1 Bob St", "Springfield", "12345", 40.7100, -74.0000);

        Customer alice = service.registerCustomer("Alice", "123-4567", aliceAddr);
        Restaurant pizza = service.registerRestaurant("Pizza Palace", pizzaAddr);
        Restaurant burger = service.registerRestaurant("Burger Barn",  burgerAddr);
        service.registerRestaurant("Taco Town", tacoAddr);
        service.registerDeliveryAgent("Bob", "321-7654", bobAddr);

        pizza.addToMenu(new MenuItem("P001", "Margherita Pizza", 12.99));
        pizza.addToMenu(new MenuItem("P002", "Veggie Pizza",     11.99));
        burger.addToMenu(new MenuItem("B001", "Classic Burger",   8.99));

        // ── Search ────────────────────────────────────────────────────────────
        System.out.println("\n=== Restaurants in Springfield ===");
        service.searchRestaurants(List.of(new SearchByCityStrategy("Springfield")))
                .forEach(r -> System.out.println("  " + r.getName()));

        System.out.println("\n=== Near Alice serving Pizza ===");
        service.searchRestaurants(List.of(new SearchByProximityStrategy(aliceAddr, 0.02), new SearchByMenuKeywordStrategy("Pizza")))
                .forEach(r -> System.out.printf("  %s (%.4f away) \n",
                r.getName(), aliceAddr.distanceTo(r.getAddress())));

        // ── Browse menu ───────────────────────────────────────────────────────
        System.out.println("\n=== Pizza Palace Menu ===");
        service.getRestaurantMenu(pizza.getId())
                .getItems()
                .forEach(i -> System.out.printf("  %s: $%.2f \n", i.getName(), i.getPrice()));

        // ── Happy path ────────────────────────────────────────────────────────
        System.out.println("\n=== Placing Order ===");
        MenuItem chosen = pizza.getMenu().getItem("P001");
        Order order = service.placeOrder(alice.getId(), pizza.getId(), List.of(new OrderItem(chosen, 2)));

        System.out.println("\n=== CONFIRMED -> PREPARING ===");
        service.startPreparingOrder(order.getId());

        System.out.println("\n=== PREPARING -> READY_FOR_PICKUP (agent assignment triggered) ===");
        service.markOrderReadyForPickup(order.getId());

        System.out.println("\n=== OUT_FOR_DELIVERY -> DELIVERED (agent released) ===");
        service.markOrderDelivered(order.getId());

        // ── State guard ───────────────────────────────────────────────────────
        System.out.println("\n=== Illegal cancel on DELIVERED order (expect exception) ===");
        try {
            service.cancelOrder(order.getId());
        } catch (IllegalStateTransitionException e) {
            System.out.println("[BLOCKED] " + e.getMessage());
        }

        // ── Cancel during PREPARING ───────────────────────────────────────────
        System.out.println("\n=== Cancel during PREPARING ===");
        Order order2 = service.placeOrder(alice.getId(), pizza.getId(), List.of(new OrderItem(chosen, 1)));
        service.startPreparingOrder(order2.getId());
        service.cancelOrder(order2.getId());
        System.out.println("Order2 final status: " + order2.getStatus());
    }
}