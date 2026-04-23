package inventoryManagement.restock;

import inventoryManagement.exceptions.ProductNotFoundException;
import inventoryManagement.warehouse.ProductStock;
import inventoryManagement.warehouse.Warehouse;
import lombok.Setter;

import java.util.List;

/**
 * Owns the restock orchestration logic.
 * Separated from InventoryStockManager (SRP): the manager coordinates the system; the orchestrator drives restock decisions.
 *
 * RestockStrategy is volatile so strategy swaps are visible across threads without requiring a lock on the entire orchestrator.
 */
@Setter
public class RestockOrchestrator {
    private volatile RestockStrategy strategy;

    public RestockOrchestrator(RestockStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Checks all warehouses and restocks any SKU below its threshold.
     */
    public void performFullCheck(List<Warehouse> warehouses) {
        for (Warehouse warehouse : warehouses) {
            for (ProductStock stock : warehouse.getAllStock()) {
                if (stock.isBelowThreshold()) {
                    strategy.restock(stock);
                }
            }
        }
    }

    /**
     * Checks a specific SKU across all warehouses.
     * Throws ProductNotFoundException if the SKU is not found in any warehouse.
     */
    public void checkAndRestock(String sku, List<Warehouse> warehouses) {
        boolean found = false;
        for (Warehouse warehouse : warehouses) {
            ProductStock stock = warehouse.getStock(sku);
            if (stock != null) {
                found = true;
                if (stock.isBelowThreshold()) {
                    strategy.restock(stock);
                }
            }
        }
        if (!found) {
            throw new ProductNotFoundException(sku);
        }
    }
}
