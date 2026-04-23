package foodDelivery.strategy.assignment;

import foodDelivery.models.DeliveryAgent;
import foodDelivery.order.Order;

import java.util.List;
import java.util.Optional;

public interface DeliveryAssignmentStrategy {
    Optional<DeliveryAgent> findAgent(Order order, List<DeliveryAgent> agents);
}
