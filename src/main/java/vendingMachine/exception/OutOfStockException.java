package vendingMachine.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String productName) {
        super("Product out of stock: " + productName);
    }
}
