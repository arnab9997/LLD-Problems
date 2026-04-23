package parkingLot.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import parkingLot.enums.PaymentStatus;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Payment {
    private final UUID id;      // For idempotency and retries
    private final Double amount;
    private PaymentStatus paymentStatus;

    public Payment(double amount) {
        this.id = UUID.randomUUID();
        this.amount = amount;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public void markCompleted() {
        this.paymentStatus = PaymentStatus.COMPLETED;
    }
}
