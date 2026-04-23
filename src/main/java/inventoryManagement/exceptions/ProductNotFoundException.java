package inventoryManagement.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String sku) {
        super("Product not found for SKU: " + sku);
    }
}
