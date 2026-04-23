package parkingLot.models;

import lombok.Getter;
import lombok.Setter;
import parkingLot.enums.TicketStatus;
import parkingLot.exception.IllegalStateTransitionException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class Ticket {
    private final UUID id;
    private final int spotID;
    private final String vehicleNumber;
    private final OffsetDateTime entryTime;
    private OffsetDateTime exitTime;
    private TicketStatus ticketStatus;

    public Ticket(UUID id, int spotID, String vehicleNumber, OffsetDateTime entryTime) {
        this.id = id;
        this.spotID = spotID;
        this.vehicleNumber = vehicleNumber;
        this.entryTime = entryTime;
        this.ticketStatus = TicketStatus.ACTIVE;
    }

    // State transition: ACTIVE -> PAID
    public void markPaid(OffsetDateTime exitTime) {
        if (!TicketStatus.ACTIVE.equals(ticketStatus)) {
            throw new IllegalStateTransitionException(ticketStatus, TicketStatus.PAID);
        }

        if (exitTime == null || exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Invalid exit time");
        }

        this.exitTime = exitTime;
        this.ticketStatus = TicketStatus.PAID;
    }

    // State transition: PAID -> COMPLETED
    public void markCompleted() {
        if (!TicketStatus.PAID.equals(ticketStatus)) {
            throw new IllegalStateTransitionException(ticketStatus, TicketStatus.COMPLETED);
        }

        this.ticketStatus = TicketStatus.COMPLETED;
    }
}
