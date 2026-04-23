package inventoryManagement.Products;

import inventoryManagement.enums.ProductCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClothingProduct extends Product {
    private String size;
    private String color;

    public ClothingProduct(String sku, String name, double price, String size, String color) {
        super(sku, name, price, ProductCategory.CLOTHING);
        this.size = size;
        this.color = color;
    }
}
