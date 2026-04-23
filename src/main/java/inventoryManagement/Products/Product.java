package inventoryManagement.Products;

import inventoryManagement.enums.ProductCategory;
import lombok.Getter;

/**
 *  Immutable - quantity and threshold are warehouse-level concerns, not product-level concerns. No setters exposed.
 */
@Getter
public class Product {
    protected String sku;
    protected String name;
    protected double price;
    private final ProductCategory productCategory;

    public Product(String sku, String name, double price, ProductCategory productCategory) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.productCategory = productCategory;
    }
}
