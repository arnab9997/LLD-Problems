package vendingMachine.exception;

public class InsufficientAmountInsertedException extends RuntimeException {
    public InsufficientAmountInsertedException(int insertedAmount, int requiredAmount) {
        super("Required amount: " + requiredAmount + ", but inserted: " + insertedAmount);
    }
}
