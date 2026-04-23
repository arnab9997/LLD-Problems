## Functional Requirements
* Each warehouse maintains independent stock levels and thresholds per SKU.
* Each product has attributes like SKU (Stock Keeping Unit), name, price, quantity.
* Add stock for a product in a warehouse.
* Remove stock with proper validation.
  * Sufficient quantity
  * Product with SKU exists
* Track stock levels and identify items below minimum threshold.
* Different stocking strategies:

Note: SKU (Stock Keeping Unit) = a unique, identifier for a product. Think of it as:
“The system’s way of saying this exact thing, not something that sounds like it.”
Example:
    SKU: TSHIRT-WHITE-M-001
    Name: T-Shirt
One name → many SKUs.

---

## Non-functional requirements
* Thread-safe operations:
  * Concurrent reads/writes across warehouses.
* Scalability:
  * Handle increasing warehouses and SKUs efficiently.
* Extensibility:
  * Easy to add new product types and restock strategies.

---

## Core entities
* Product
* Warehouse
* ProductStock
* InventoryStockManager
* RestockOrchestrator

---

## Enums
* ProductCategory

---

## State Models
N/A

---

## Design Patterns
* Singleton Design Pattern - For restocking products
* Orchestrator Patten - Separates business logic of restocking from coordination logic (manager)

---

## API Design
* addWarehouse(Warehouse warehouse)
* removeWarehouse(Warehouse warehouse)
* getWarehouses()
* performInventoryCheck()
* checkAndRestock(String sku)
* addStock(Product product, int quantity, int threshold)
* removeStock(String sku, int quantity)

---

## DB Persistence
* Warehouses
* Products
* Inventory(warehouse_id, sku, quantity, threshold)

---

## Notes
* Concurrency Design Choices
  * ConcurrentHashMap -> safe concurrent updates
  * compute() -> atomic check-then-act
  * AtomicInteger -> lock-free updates
  * CopyOnWriteArrayList -> optimized for read-heavy workloads
* Why Product is (supposed to be) immutable
  * Prevents inconsistent shared state across warehouses
  * Keeps inventory concerns separate