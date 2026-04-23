package vendingMachine.state;

import vendingMachine.model.Product;
import vendingMachine.model.Transaction;
import vendingMachine.VendingMachineContext;

/**
 * NOTE: Transaction lifecycle begins here
 */
public class ProductSelectionState implements VendingMachineState {

    @Override
    public void selectProduct(VendingMachineContext context, int productCode) {
        Product product = context.selectProductToPurchase(productCode);
        Transaction txn = new Transaction(product);
        txn.markPaymentInProgress();
        context.setTransaction(txn);
        context.transitionTo(new AcceptAmountState());
    }

    /**
     * No product selected, no coins inserted — just close the session
     */
    @Override
    public void cancel(VendingMachineContext context) {
        context.transitionTo(new IdleState());
    }
}
