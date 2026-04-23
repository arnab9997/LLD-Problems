package vendingMachine.state;

import vendingMachine.VendingMachineContext;

/**
 * Machine is idle and ready to start a new session.
 * Only startSession() is valid; all user-flow operations throw.
 */
public class IdleState implements VendingMachineState {

    @Override
    public void startSession(VendingMachineContext context) {
        context.transitionTo(new ProductSelectionState());
    }
}
