package vendingMachine.state;

import vendingMachine.model.Transaction;
import vendingMachine.VendingMachineContext;
import vendingMachine.enums.Coin;
import vendingMachine.exception.ChangeNotAvailableException;
import vendingMachine.exception.OutOfStockException;

import java.util.Map;

/**
 * Sufficient funds have been inserted.
 * Dispense triggers <<stock deduction + change calculation + change removal>> sequence
 */
public class DispensingState implements VendingMachineState {

    @Override
    public void dispenseProduct(VendingMachineContext context) {
        Transaction txn = context.getTransaction();
        try {
            Map<Coin, Integer> change = context.executeDispense();
            txn.markCompleted();
            System.out.println("Dispensing: " + txn.getProduct().getName());
            if (!change.isEmpty()) {
                System.out.println("Returning change: " + change);
            }
            context.transitionTo(new IdleState());
        } catch (ChangeNotAvailableException | OutOfStockException e) {
            txn.markFailed();
            context.refundInsertedCoins();
            context.resetToIdle();
            throw e;
        }
    }
}
