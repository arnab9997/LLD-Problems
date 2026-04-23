package vendingMachine.model;

import lombok.Getter;
import lombok.Setter;
import vendingMachine.enums.Coin;
import vendingMachine.enums.ProductTransactionState;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class Transaction {
    private final String transactionID;
    private final Product product;
    // insertedCoins tracked as a Map (not just a total) so we can refund the EXACT denominations the customer inserted, not a re-calculated approximation.
    private final Map<Coin, Integer> insertedCoins;
    private ProductTransactionState state;

    public Transaction(Product product) {
        this.transactionID = UUID.randomUUID().toString();
        this.product = product;
        insertedCoins = new EnumMap<>(Coin.class);
        this.state = ProductTransactionState.INITIATED;
    }

    public void markPaymentInProgress() {
        this.state = ProductTransactionState.PAYMENT_IN_PROGRESS;
    }

    public void markDispensing() {
        this.state = ProductTransactionState.DISPENSING;
    }

    public void markCompleted() {
        this.state = ProductTransactionState.COMPLETED;
    }

    public void markFailed() {
        this.state = ProductTransactionState.FAILED;
    }

    public void markCancelled() {
        this.state = ProductTransactionState.CANCELLED;
    }

    /**
     * Accumulates coins across multiple insertions.
     * Returns the new running total inserted so far.
     */
    public int addCoins(Map<Coin, Integer> coins) {
        coins.forEach((coin, qty) -> insertedCoins.merge(coin, qty, Integer::sum));
        return getInsertedAmount();
    }

    public int getInsertedAmount() {
        return insertedCoins.entrySet().stream()
                .mapToInt(e -> e.getKey().getValue() * e.getValue())
                .sum();
    }
}
