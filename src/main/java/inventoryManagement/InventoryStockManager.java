package inventoryManagement;

import inventoryManagement.restock.RestockOrchestrator;
import inventoryManagement.restock.RestockStrategy;
import inventoryManagement.warehouse.Warehouse;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * System coordinator: owns the warehouse registry and delegates restock decisions to RestockOrchestrator.
 *
 * Singleton with DCL + volatile instance field.
 * CopyOnWriteArrayList for warehouses: warehouse additions/removals are rare;
 * reads (during inventory checks) are frequent - COW is the right trade-off.
 */
public class InventoryStockManager {

    private static volatile InventoryStockManager instance;

    private final CopyOnWriteArrayList<Warehouse> warehouses = new CopyOnWriteArrayList<>();
    private final RestockOrchestrator restockOrchestrator;

    private InventoryStockManager(RestockStrategy initialStrategy) {
        this.restockOrchestrator = new RestockOrchestrator(initialStrategy);
    }

    public static InventoryStockManager getInstance(RestockStrategy initialStrategy) {
        if (instance == null) {
            synchronized (InventoryStockManager.class) {
                if (instance == null) {
                    instance = new InventoryStockManager(initialStrategy);
                }
            }
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    // Warehouse registry
    // -------------------------------------------------------------------------

    public void addWarehouse(Warehouse warehouse) {
        warehouses.addIfAbsent(warehouse);
    }

    public void removeWarehouse(Warehouse warehouse) {
        warehouses.remove(warehouse);
    }

    public List<Warehouse> getWarehouses() {
        return Collections.unmodifiableList(warehouses);
    }

    // -------------------------------------------------------------------------
    // Restock delegation — manager does not know strategy internals
    // -------------------------------------------------------------------------

    public void setRestockStrategy(RestockStrategy strategy) {
        restockOrchestrator.setStrategy(strategy);
    }

    public void performInventoryCheck() {
        restockOrchestrator.performFullCheck(warehouses);
    }

    public void checkAndRestock(String sku) {
        restockOrchestrator.checkAndRestock(sku, warehouses);
    }
}