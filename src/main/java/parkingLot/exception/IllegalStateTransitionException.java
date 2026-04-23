package parkingLot.exception;


import parkingLot.enums.TicketStatus;

public class IllegalStateTransitionException extends RuntimeException {
    public IllegalStateTransitionException(TicketStatus from, TicketStatus to) {
        super("Illegal ticket state transition: " + from + " -> " + to);
    }
}
