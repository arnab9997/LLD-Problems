package vendingMachine;

import vendingMachine.enums.Coin;
import vendingMachine.model.Product;
import vendingMachine.model.Transaction;
import vendingMachine.state.VendingMachineState;

import java.util.Map;

/**
 * Narrow interface that VendingMachineState implementations are allowed to use.
 *
 * VendingMachineState depend on this abstraction, and not on the full VendingMachine class. This means:
 *   - States cannot accidentally call admin methods (addProduct, refillCoins)
 *   - VendingMachine can be tested behind this interface
 *   - The dependency arrow is: State → VendingMachineContext ← VendingMachine
 */
public interface VendingMachineContext {

    /** Transition to a new state. The only sanctioned way to change state. */
    void transitionTo(VendingMachineState newState);

    /** Look up a product and validate it is in stock. */
    Product selectProductToPurchase(int productCode);

    /** Bind a newly created transaction to this machine context. */
    void setTransaction(Transaction transaction);

    /** Retrieve the current active transaction. */
    Transaction getTransaction();

    /**
     * Deduct product stock, calculate change from overpayment, remove
     * those coins from inventory, and return the change breakdown.
     */
    Map<Coin, Integer> executeDispense();

    /**
     * Return the exact coins the customer inserted back to inventory removal.
     * Uses the transaction's insertedCoins map — not a re-calculated approximation.
     */
    void refundInsertedCoins();

    /** Refund if any coins were inserted, then clear the transaction and go Idle. */
    void resetToIdle();
}
