package foodDelivery.strategy.assignment;

import foodDelivery.models.Address;
import foodDelivery.models.DeliveryAgent;
import foodDelivery.order.Order;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NearestAvailableAgentStrategy implements DeliveryAssignmentStrategy {

    /**
     * Thread-safety design — three-step pipeline:
     *
     *   1. filter(isAvailable)       hint-based pre-filter; non-binding, a race here is harmless
     *   2. sorted by totalDistance   determines preference order among candidates
     *   3. filter(tryClaimForDelivery) atomic CAS gate — only one thread wins per agent
     *
     * Two threads racing for the same agent:
     *   Thread A: CAS true->false succeeds -> agent returned
     *   Thread B: CAS fails               -> agent dropped by filter, tries next in sorted order
     *
     * This eliminates the TOCTOU race when: isAvailable() check + setAvailable(false)
     * are two separate non-atomic operations. tryClaimForDelivery() collapses them into one.
     */
    @Override
    public Optional<DeliveryAgent> findAgent(Order order, List<DeliveryAgent> agents) {
        Address restaurantAddr = order.getRestaurant().getAddress();
        Address customerAddr   = order.getCustomer().getAddress();

        return agents.stream()
                .filter(DeliveryAgent::isAvailable)
                .sorted(Comparator.comparingDouble(
                        a -> totalDistance(a, restaurantAddr, customerAddr)))
                .filter(DeliveryAgent::tryClaimForDelivery)
                .findFirst();
    }

    private double totalDistance(DeliveryAgent agent, Address restaurant, Address customer) {
        // Agent -> Restaurant -> Customer: minimises total travel time
        return agent.getCurrentLocation().distanceTo(restaurant)
                + restaurant.distanceTo(customer);
    }
}