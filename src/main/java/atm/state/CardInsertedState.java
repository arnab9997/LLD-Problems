package atm.state;

import atm.ATMMachine;

/**
 * Card is inserted. ATM is waiting for PIN entry.
 * Supports enterPin and ejectCard
 */
public class CardInsertedState implements ATMState {

    private int pinAttempts = 0;
    private static final int MAX_PIN_ATTEMPTS = 3;

    @Override
    public void enterPin(ATMMachine machine, String pin) {
        if (machine.authenticate(machine.getCurrentCard(), pin)) {
            System.out.println("PIN verified.");
            machine.transitionTo(new SelectOperationState());
        } else {
            pinAttempts++;
            System.out.println("Incorrect PIN. Attempts: " + pinAttempts + "/" + MAX_PIN_ATTEMPTS);
            if (pinAttempts >= MAX_PIN_ATTEMPTS) {
                machine.blockCard();
                machine.resetSession();
            }
        }
    }

    @Override
    public void ejectCard(ATMMachine machine) {
        System.out.println("Card ejected by user.");
        machine.resetSession();
    }
}
