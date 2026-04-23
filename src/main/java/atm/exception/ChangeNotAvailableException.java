package atm.exception;

public class ChangeNotAvailableException extends RuntimeException {
    public ChangeNotAvailableException(int amount) {
        super("Cannot dispense amount: $" + amount + ". Either not a valid denomination multiple or insufficient notes in the machine.");
    }
}
