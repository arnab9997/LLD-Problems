package foodDelivery.order;

import foodDelivery.models.MenuItem;
import lombok.Getter;

@Getter
public class OrderItem {
    private final MenuItem item;
    private final int quantity;

    public OrderItem(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }
}
