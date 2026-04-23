package atm.state;

import atm.ATMMachine;
import atm.enums.OperationType;
import atm.exception.InsufficientBalanceException;

/**
 * A monetary operation has been selected. Collects an amount and executes.
 */
public class TransactionState implements ATMState {

    /** Operation is final and set at construction, so is never null. */
    private final OperationType op;

    public TransactionState(OperationType op) {
        this.op = op;
    }

    @Override
    public void performTransaction(ATMMachine machine, int amount) {
        try {
            switch (op) {
                case WITHDRAW_CASH -> handleWithdrawal(machine, amount);
                case DEPOSIT_CASH  -> handleDeposit(machine, amount);
            }
        } catch (InsufficientBalanceException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
        // Always return to menu — whether success, validation failure, or exception.
        machine.transitionToMenu();
    }

    @Override
    public void ejectCard(ATMMachine machine) {
        System.out.println("Card ejected by user.");
        machine.resetSession();
    }

    private void handleWithdrawal(ATMMachine machine, int amount) {
        if (!machine.isValidWithdrawalAmount(amount)) {
            System.out.println("Invalid amount. Please enter a multiple of $20.");
            return;
        }
        if (!machine.canDispenseCash(amount)) {
            System.out.println("Insufficient notes in machine. Please try a lower amount.");
            return;
        }
        if (machine.getBalance() < amount) {
            System.out.println("Transaction failed: Insufficient balance in account.");
            return;
        }

        /*
         * Dispense-before-debit.
         *
         * Wrong order:
         *   machine.debitAmount(amount);   // account debited
         *   machine.dispenseCash(amount);  // if this throws → money gone, no cash
         *
         * Correct order:
         *   machine.dispenseCash(amount);  // notes physically ejected first
         *   machine.debitAmount(amount);   // account reflects the completed dispense
         *
         * dispenseCash() is safe here because canDispenseCash() already confirmed dispensability above.
         */
        machine.dispenseCash(amount);
        machine.debitAmount(amount);
    }

    private void handleDeposit(ATMMachine machine, int amount) {
        machine.creditAmount(amount);
        System.out.println("Deposited $" + amount + ". New balance: $" + machine.getBalance());
    }
}
