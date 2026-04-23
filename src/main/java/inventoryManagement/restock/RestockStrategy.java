package inventoryManagement.restock;

import inventoryManagement.warehouse.ProductStock;

public interface RestockStrategy {
    void restock(ProductStock productStock);
}
