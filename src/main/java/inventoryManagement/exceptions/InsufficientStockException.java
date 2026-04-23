package inventoryManagement.exceptions;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String sku, int requested, int available) {
        super("Insufficient stock for SKU: " + sku + ". Requested: " + requested + ", Available: " + available);
    }
}
