package vendingMachine.exception;

public class ChangeNotAvailableException extends RuntimeException {
    public ChangeNotAvailableException(int changeAmount) {
        super("Cannot provide exact change for amount: " + changeAmount);
    }
}
