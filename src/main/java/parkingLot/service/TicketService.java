package parkingLot.service;

import parkingLot.enums.TicketStatus;
import parkingLot.exception.InvalidTicketException;
import parkingLot.models.Ticket;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TicketService {
    private final ConcurrentMap<UUID, Ticket> tickets = new ConcurrentHashMap<>();

    public Ticket createTicket(int spotId, String vehicleNumber) {
        Ticket ticket = new Ticket(UUID.randomUUID(), spotId, vehicleNumber, OffsetDateTime.now());
        tickets.put(ticket.getId(), ticket);
        return ticket;
    }

    public Optional<Ticket> getTicket(UUID ticketId) {
        return Optional.ofNullable(tickets.get(ticketId));
    }

    public Ticket markPaid(UUID ticketId, OffsetDateTime exitTime) {
        // If already PAID, this is idempotent retry
        // If COMPLETED, cannot reprocess
        // Mark as paid (mutates the ticket)

        Ticket ticket = tickets.compute(ticketId, (id, existing) -> {
            if (existing == null) {
                throw new InvalidTicketException(ticketId);
            }
            if (existing.getTicketStatus() == TicketStatus.PAID) {
                return existing;  // Idempotent
            }
            if (existing.getTicketStatus() == TicketStatus.COMPLETED) {
                throw new InvalidTicketException(ticketId);
            }
            existing.markPaid(exitTime);
            return existing;
        });

        if (ticket == null) {
            throw new InvalidTicketException(ticketId);
        }
        return ticket;
    }

    public void completeTicket(UUID ticketId) {
        Ticket removed = tickets.remove(ticketId);
        if (removed == null || removed.getTicketStatus() != TicketStatus.PAID) {
            throw new InvalidTicketException(ticketId);
        }
        removed.markCompleted();
    }
}
