package vendingMachine;

import vendingMachine.strategy.changeCalculator.ChangeCalculationStrategy;
import vendingMachine.enums.Coin;
import vendingMachine.exception.OutOfStockException;
import vendingMachine.inventory.CoinInventory;
import vendingMachine.inventory.ProductInventory;
import vendingMachine.model.Product;
import vendingMachine.model.Transaction;
import vendingMachine.state.IdleState;
import vendingMachine.state.VendingMachineState;

import java.util.List;
import java.util.Map;

public class VendingMachine implements VendingMachineContext {
    private final CoinInventory coinInventory;
    private final ProductInventory productInventory;
    private final ChangeCalculationStrategy changeCalculationStrategy;

    private VendingMachineState currentState;
    private Transaction transaction;

    public VendingMachine(CoinInventory coinInventory, ProductInventory productInventory, ChangeCalculationStrategy changeCalculationStrategy) {
        this.coinInventory = coinInventory;
        this.productInventory = productInventory;
        this.changeCalculationStrategy = changeCalculationStrategy;
        this.currentState = new IdleState();
    }

    // =================== VendingMachineContext implementation (used by states) ===================

    @Override
    public void transitionTo(VendingMachineState newState) {
        System.out.println("[State] " + currentState.stateName() + " → " + newState.stateName());
        this.currentState = newState;
    }

    @Override
    public Product selectProductToPurchase(int productCode) {
        Product product = productInventory.getProduct(productCode);
        if (product.getQuantity() == 0) {
            throw new OutOfStockException(product.getName());
        }
        return product;
    }

    @Override
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public Transaction getTransaction() {
        return this.transaction;
    }

    @Override
    public Map<Coin, Integer> executeDispense() {
        // change calculation -> change deduction -> stock deduction
        int insertedAmount = transaction.getInsertedAmount();
        int price = transaction.getProduct().getPrice();
        int changeToDispense = insertedAmount - price;
        Map<Coin, Integer> changeMap = Map.of();

        if (changeToDispense > 0) {
            // calculateChange may throw ChangeNotAvailableException — caller (DispensingState) handles it
            changeMap = changeCalculationStrategy.calculateChange(changeToDispense);
        }

        transaction.getProduct().reduceQuantity();
        if (!changeMap.isEmpty()) {
            coinInventory.removeCoins(changeMap);
        }

        return changeMap;
    }

    @Override
    public void refundInsertedCoins() {
        if (transaction == null) {
            return;
        }

        Map<Coin, Integer> inserted = transaction.getInsertedCoins();
        if (!inserted.isEmpty()) {
            coinInventory.removeCoins(inserted);
            System.out.println("Refunding coins: " + inserted);
        }
    }

    @Override
    public void resetToIdle() {
        this.transaction = null;
        this.currentState = new IdleState();
    }

    // =================== Customer-facing API (delegates to current state) ===================

    public void startSession() {
        currentState.startSession(this);
    }

    public void selectProduct(int productCode) {
        currentState.selectProduct(this, productCode);
    }

    public void insertCoins(Map<Coin, Integer> coins) {
        currentState.insertCoins(this, coins);
        // After inserting coins, if we're still in AcceptAmountState, print balance
        if (transaction != null) {
            int inserted = transaction.getInsertedAmount();
            int required = transaction.getProduct().getPrice();
            if (inserted < required) {
                System.out.println("Inserted: " + inserted + " / Required: " + required
                        + " - please insert " + (required - inserted) + " more.");
            }
        }
    }

    public void dispenseProduct() {
        currentState.dispenseProduct(this);
    }

    public void cancel() {
        currentState.cancel(this);
    }

    public List<Product> displayProducts() {
        return productInventory.listAllProducts();
    }

    // =================== Admin-facing API ===================

    public void addProduct(Product product) {
        productInventory.addProduct(product);
    }

    public void restockProduct(int productCode, int quantity) {
        productInventory.getProduct(productCode).addQuantity(quantity);
    }

    public void refillCoins(Map<Coin, Integer> coins) {
        coinInventory.addCoins(coins);
    }
}
