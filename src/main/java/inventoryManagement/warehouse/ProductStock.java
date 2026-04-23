package inventoryManagement.warehouse;

import inventoryManagement.Products.Product;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class ProductStock {
    private final Product product;
    private final AtomicInteger quantity;
    private final int minStockLevel;

    public ProductStock(Product product, int initialQuantity, int minStockLevel) {
        this.product = product;
        this.quantity = new AtomicInteger(initialQuantity);
        this.minStockLevel = minStockLevel;
    }

    public void addStock(int amount) {
        quantity.addAndGet(amount);
    }

    public int getQuantity() {
        return quantity.get();
    }

    /**
     * CAS-based decrement: retries until it either succeeds or detects insufficient stock.
     * Atomic - no external lock needed for this operation.
     *
     * @return true if stock was successfully removed
     */
    public boolean removeStock(int amount) {
        while (true) {
            int currentAmount = quantity.get();
            if (currentAmount < amount) {
                return false;
            }
            if (quantity.compareAndSet(currentAmount, currentAmount - amount)) {
                return true;
            }
        }
    }

    public boolean isBelowThreshold() {
        return quantity.get() < minStockLevel;
    }
}
