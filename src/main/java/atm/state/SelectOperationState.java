package atm.state;

import atm.ATMMachine;
import atm.enums.OperationType;

/**
 * PIN authenticated. User selects what they want to do.
 *
 * Non-monetary operations (CHECK_BALANCE) are executed immediately here.
 * Monetary operations (WITHDRAW, DEPOSIT) transition to TransactionState.
 *  - OperationType is passed directly into TransactionState's constructor, so ATMMachine doesn't need
 *    a nullable selectedOperation field. The operation is explicit and owned by the state that needs it.
 */
public class SelectOperationState implements ATMState {

    @Override
    public void selectOperation(ATMMachine machine, OperationType op) {
        System.out.println("Operation selected: " + op);

        switch (op) {
            case CHECK_BALANCE -> {
                System.out.println("Current balance: $" + machine.getBalance());
                // Stay in SelectOperationState — user can pick another operation.
            }
            case WITHDRAW_CASH, DEPOSIT_CASH -> {
                // Amount required — hand off to TransactionState.
                machine.transitionTo(new TransactionState(op));
            }
            default -> System.out.println("Operation not yet supported: " + op);
        }
    }

    @Override
    public void ejectCard(ATMMachine machine) {
        System.out.println("Card ejected by user.");
        machine.resetSession();
    }
}
