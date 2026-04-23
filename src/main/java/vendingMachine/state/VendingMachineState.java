package vendingMachine.state;

import vendingMachine.VendingMachineContext;
import vendingMachine.enums.Coin;
import vendingMachine.exception.InvalidStateTransitionException;

import java.util.Map;

/**
 * Model the methods in the "state machine" from the system flow, not the user’s flow.
 * Humans love imagining everything from their POV, but software actually cares about events that hit the system. State machines react to events, not to user's journey.
 * Basically: “What events can the machine receive while it is in some state?”
 */
public interface VendingMachineState {
    default void startSession(VendingMachineContext context) {
        throw new InvalidStateTransitionException("startSession", stateName());
    }

    default void selectProduct(VendingMachineContext context, int productCode) {
        throw new InvalidStateTransitionException("selectProduct", stateName());
    }

    default void insertCoins(VendingMachineContext context, Map<Coin, Integer> coins) {
        throw new InvalidStateTransitionException("insertCoins", stateName());
    }

    default void dispenseProduct(VendingMachineContext context) {
        throw new InvalidStateTransitionException("dispenseProduct", stateName());
    }

    default void cancel(VendingMachineContext context) {
        throw new InvalidStateTransitionException("cancel", stateName());
    }

    default String stateName() {
        return this.getClass().getSimpleName();
    }
}
