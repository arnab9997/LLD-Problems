package inventoryManagement;

import inventoryManagement.Products.ClothingProduct;
import inventoryManagement.Products.ElectronicsProduct;
import inventoryManagement.Products.GroceryProduct;
import inventoryManagement.Products.Product;
import inventoryManagement.exceptions.InsufficientStockException;
import inventoryManagement.exceptions.ProductNotFoundException;
import inventoryManagement.restock.BulkRestockStrategy;
import inventoryManagement.restock.JustInTimeRestockStrategy;
import inventoryManagement.warehouse.Warehouse;

import java.time.LocalDate;

public class InventoryManagementDemo {
    public static void main(String[] args) {

        // --- Bootstrap ---
        InventoryStockManager manager = InventoryStockManager.getInstance(new JustInTimeRestockStrategy());

        Warehouse warehouse1 = new Warehouse("East Coast Warehouse");
        Warehouse warehouse2 = new Warehouse("West Coast Warehouse");
        manager.addWarehouse(warehouse1);
        manager.addWarehouse(warehouse2);

        // --- Create immutable product catalog entries directly ---
        Product laptop = new ElectronicsProduct("SKU-001", "Laptop", 1000.0, "Dell", 24);
        Product tShirt = new ClothingProduct("SKU-002", "T-Shirt", 20.0, "M", "White");
        Product apple  = new GroceryProduct("SKU-003", "Apple", 1.0, LocalDate.now().plusDays(30), false);

        // --- Add stock (quantity + minStockLevel are warehouse-level concerns, not on Product) ---
        warehouse1.addStock(laptop, 15, 25);    // qty=15, minStockLevel=25 → below threshold, should restock
        warehouse1.addStock(tShirt, 120, 100);  // qty=120, minStockLevel=100 → healthy, no restock
        warehouse2.addStock(apple, 50, 200);    // qty=50,  minStockLevel=200 → below threshold, should restock

        System.out.println("\n--- Performing full inventory check with JIT strategy ---");
        manager.performInventoryCheck();

        System.out.println("\n--- Switching to Bulk strategy and checking laptop specifically ---");
        manager.setRestockStrategy(new BulkRestockStrategy());
        manager.checkAndRestock("SKU-001");

        // --- Demonstrate typed exceptions ---
        System.out.println("\n--- Demonstrating exception handling ---");
        try {
            warehouse1.removeStock("SKU-001", 99999);
        } catch (InsufficientStockException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }

        try {
            manager.checkAndRestock("SKU-UNKNOWN");
        } catch (ProductNotFoundException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }
    }
}