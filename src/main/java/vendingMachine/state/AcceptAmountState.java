package vendingMachine.state;

import vendingMachine.model.Transaction;
import vendingMachine.VendingMachineContext;
import vendingMachine.enums.Coin;

import java.util.Map;

/**
 * Sole responsibility: accept coins.
 * Transitions to DispensingState once insertedAmount >= price.
 */
public class AcceptAmountState implements VendingMachineState {

    @Override
    public void insertCoins(VendingMachineContext context, Map<Coin, Integer> coins) {
        Transaction txn = context.getTransaction();
        int totalAmountInserted = txn.addCoins(coins);
        int requiredAmount = txn.getProduct().getPrice();

        if (totalAmountInserted >= requiredAmount) {
            txn.markDispensing();
            context.transitionTo(new DispensingState());
        }
    }

    @Override
    public void cancel(VendingMachineContext context) {
        context.refundInsertedCoins();
        context.getTransaction().markCancelled();
        context.transitionTo(new IdleState());
    }
}
