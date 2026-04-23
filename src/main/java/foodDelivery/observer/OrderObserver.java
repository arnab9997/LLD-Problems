package foodDelivery.observer;

import foodDelivery.order.Order;

public interface OrderObserver {
    void onOrderUpdated(Order order);
}
