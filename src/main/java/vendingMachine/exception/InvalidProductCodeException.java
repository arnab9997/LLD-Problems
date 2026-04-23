package vendingMachine.exception;

public class InvalidProductCodeException extends RuntimeException {
    public InvalidProductCodeException(int productCode) {
        super("Invalid product code: " + productCode);
    }
}
