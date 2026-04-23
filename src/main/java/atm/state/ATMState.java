package atm.state;

import atm.ATMMachine;
import atm.enums.OperationType;
import atm.exception.InvalidStateTransitionException;

public interface ATMState {

    default void insertCard(ATMMachine machine, String cardNumber) {
        throw new InvalidStateTransitionException("insertCard", stateName());
    }

    default void enterPin(ATMMachine machine, String pin) {
        throw new InvalidStateTransitionException("enterPin", stateName());
    }

    default void selectOperation(ATMMachine machine, OperationType op) {
        throw new InvalidStateTransitionException("selectOperation", stateName());
    }

    default void performTransaction(ATMMachine machine, int amount) {
        throw new InvalidStateTransitionException("performTransaction", stateName());
    }

    default void ejectCard(ATMMachine machine) {
        throw new InvalidStateTransitionException("ejectCard", stateName());
    }

    default String stateName() {
        return this.getClass().getSimpleName();
    }
}
