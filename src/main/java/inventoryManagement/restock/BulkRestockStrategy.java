package inventoryManagement.restock;

import inventoryManagement.warehouse.ProductStock;

public class BulkRestockStrategy implements RestockStrategy {

    @Override
    public void restock(ProductStock productStock) {
        productStock.addStock(100);
        System.out.printf("[Bulk Restock] '%s' restocked by 100 units. New qty: %d \n", productStock.getProduct().getName(), productStock.getQuantity());
    }
}
