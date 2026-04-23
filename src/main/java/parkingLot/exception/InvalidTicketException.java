package parkingLot.exception;

import java.util.UUID;

public class InvalidTicketException extends RuntimeException {
    public InvalidTicketException(UUID ticketId) {
        super("Invalid or already processed ticket: " + ticketId);
    }
}
