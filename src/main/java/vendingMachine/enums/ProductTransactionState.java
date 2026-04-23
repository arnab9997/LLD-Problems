package vendingMachine.enums;

public enum ProductTransactionState {
    INITIATED,
    PAYMENT_IN_PROGRESS,
    DISPENSING,
    COMPLETED,
    CANCELLED,
    FAILED;
}
