package inventoryManagement.warehouse;

import inventoryManagement.Products.Product;
import inventoryManagement.exceptions.InsufficientStockException;
import inventoryManagement.exceptions.ProductNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Warehouse manages ProductStock entries keyed by SKU.
 *
 * Thread-safety:
 * - ConcurrentHashMap for the stock map (safe concurrent reads, segment-level write locking).
 * - compute() for addStock to make check-then-act atomic (eliminates TOCTOU).
 * - ProductStock.removeStock() uses CAS internally — no external lock needed.
 */
public class Warehouse {
    private final String id;
    private final String name;
    private final ConcurrentHashMap<String, ProductStock> stockMap;   // {Sku, Product}

    public Warehouse(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.stockMap = new ConcurrentHashMap<>();
    }

    /**
     * Add stock of a product. If the SKU is already tracked, increments quantity.
     * Uses compute() for atomic check-and-insert to prevent TOCTOU race.
     */
    public void addStock(Product product, int quantity, int threshold) {
        stockMap.compute(product.getSku(), (sku, existing) -> {
            if (existing == null) {
                return new ProductStock(product, quantity, threshold);
            }
            existing.addStock(quantity);
            return existing;
        });
        System.out.printf("[%s] Added %d units of '%s'. New qty: %d \n", name, quantity, product.getName(), getQuantity(product.getSku()));
    }

    /**
     * Removes stock for a SKU. Throws typed exceptions rather than returning boolean.
     * Callers can decide to catch or propagate.
     */
    public void removeStock(String sku, int quantity) {
        ProductStock stock = stockMap.get(sku);
        if (stock == null) {
            throw new ProductNotFoundException(sku);
        }
        boolean removed = stock.removeStock(quantity);
        if (!removed) {
            throw new InsufficientStockException(sku, quantity, stock.getQuantity());
        }
        System.out.printf("[%s] Removed %d units of '%s'. Remaining: %d \n", name, quantity, stock.getProduct().getName(), stock.getQuantity());
    }

    public int getQuantity(String sku) {
        ProductStock stock = stockMap.get(sku);
        return stock == null ? 0 : stock.getQuantity();
    }

    public ProductStock getStock(String sku) {
        return stockMap.get(sku);
    }

    /** Returns an unmodifiable view — prevents external mutation of the live collection. */
    public Collection<ProductStock> getAllStock() {
        return Collections.unmodifiableCollection(stockMap.values());
    }
}
