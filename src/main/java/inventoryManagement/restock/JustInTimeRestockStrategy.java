package inventoryManagement.restock;

import inventoryManagement.warehouse.ProductStock;

public class JustInTimeRestockStrategy implements RestockStrategy {

    @Override
    public void restock(ProductStock productStock) {
        int deficit = productStock.getMinStockLevel() - productStock.getQuantity();
        if (deficit <= 0) {
            return;
        }
        productStock.addStock(deficit);
        System.out.printf("[JIT Restock] '%s' restocked by %d units. New qty: %d \n", productStock.getProduct().getName(), deficit, productStock.getQuantity());
    }
}
