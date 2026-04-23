package inventoryManagement.Products;

import inventoryManagement.enums.ProductCategory;

import java.time.LocalDate;

public class GroceryProduct extends Product {
    private LocalDate expiryDate;
    private boolean refrigerated;

    public GroceryProduct(String sku, String name, double price, LocalDate expiryDate, boolean refrigerated) {
        super(sku, name, price, ProductCategory.GROCERY);
        this.expiryDate = expiryDate;
        this.refrigerated = refrigerated;
    }
}
