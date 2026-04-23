package atm.state;

import atm.ATMMachine;
import atm.model.Card;

/**
 * ATM is idle, waiting for a card to be inserted.
 * Only insertCard is valid; all other operations throw via the interface default.
 */
public class IdleState implements ATMState {

    @Override
    public void insertCard(ATMMachine machine, String cardNumber) {
        Card card = machine.findCard(cardNumber);

        if (card == null) {
            System.out.println("Card not recognized. Please retrieve your card.");
            return; // Stay in IdleState — no session started.
        }

        if (card.isCardBlocked()) {
            System.out.println("Card is blocked. Please contact your bank.");
            return; // Stay in IdleState — blocked cards never start a session.
        }

        System.out.println("Card accepted: " + cardNumber);
        machine.setCurrentCard(card);
        machine.transitionTo(new CardInsertedState());
    }
}
