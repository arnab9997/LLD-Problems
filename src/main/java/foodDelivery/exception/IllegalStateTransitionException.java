package foodDelivery.exception;

public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(String from, String to) {
        super("Illegal transition: " + from + " -> " + to);
    }
}
