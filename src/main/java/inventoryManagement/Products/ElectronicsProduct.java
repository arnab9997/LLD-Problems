package inventoryManagement.Products;

import inventoryManagement.enums.ProductCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElectronicsProduct extends Product {
    private String brand;
    private int warrantyMonths;

    public ElectronicsProduct(String sku, String name, double price, String brand, int warrantyMonths) {
        super(sku, name, price, ProductCategory.ELECTRONICS);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
    }
}
