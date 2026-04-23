package vendingMachine.exception;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String operation, String currentState) {
        super("Cannot perform '" + operation + "' in state: " + currentState);
    }
}
