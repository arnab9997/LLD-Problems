package parkingLot;

import parkingLot.enums.ParkingSpotType;
import parkingLot.exception.InvalidTicketException;
import parkingLot.exception.ParkingLotFullException;
import parkingLot.models.ParkingSpot;
import parkingLot.models.Payment;
import parkingLot.models.Ticket;
import parkingLot.models.Vehicle;
import parkingLot.service.TicketService;
import parkingLot.strategy.fees.FeeCalculationStrategy;
import parkingLot.strategy.parking.ParkingStrategy;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ParkingLot {
    private final List<ParkingLevel> levels;
    private final ParkingStrategy parkingStrategy;
    private final FeeCalculationStrategy feeCalculationStrategy;
    private final TicketService ticketService;

    public ParkingLot(List<ParkingLevel> levels, ParkingStrategy parkingStrategy, FeeCalculationStrategy feeCalculationStrategy) {
        this.levels = levels;
        this.parkingStrategy = parkingStrategy;
        this.feeCalculationStrategy = feeCalculationStrategy;
        this.ticketService = new TicketService();
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        ParkingSpotType requiredSpotType = vehicle.getVehicleType().getRequiredSpotType();
        Optional<ParkingSpot> allocatedSpot = parkingStrategy.findAndAllocateSpot(levels, requiredSpotType);

        if (allocatedSpot.isEmpty()) {
            throw new ParkingLotFullException(vehicle.getVehicleType().toString());
        }

        ParkingSpot spot = allocatedSpot.get();
        Ticket ticket = ticketService.createTicket(spot.getId(), vehicle.getVehicleNumber());

        System.out.println("Vehicle parked: " + vehicle + " at " + spot + " | Ticket: " + ticket.getId());
        return ticket;
    }

    public void exitVehicle(UUID ticketId, Vehicle vehicle) {
        // Get ticket
        Ticket ticket = ticketService.getTicket(ticketId)
                .orElseThrow(() -> new InvalidTicketException(ticketId));

        // Find spot
        ParkingSpot spot = findSpot(ticket.getSpotID())
                .orElseThrow(() -> new IllegalStateException("Spot not found: " + ticket.getSpotID()));

        // Validate vehicle
        if (!ticket.getVehicleNumber().equals(vehicle.getVehicleNumber())) {
            throw new IllegalArgumentException("Vehicle does not match ticket");
        }

        // Mark ticket paid (atomic, idempotent)
        OffsetDateTime exitTime = OffsetDateTime.now();
        Ticket paidTicket = ticketService.markPaid(ticketId, exitTime);

        // Check if already processed
        if (paidTicket.getExitTime() != null && !paidTicket.getExitTime().equals(exitTime)) {
            System.out.println("Ticket already processed (idempotent): " + ticketId);
            return;
        }

        // Calculate fee
        double feeAmount = feeCalculationStrategy.calculateFees(
                paidTicket.getEntryTime(),
                paidTicket.getExitTime(),
                spot.getParkingSpotType()
        );

        // Process payment (simplified - no strategy pattern)
        Payment payment = new Payment(feeAmount);
        System.out.println("Processing payment: $" + payment.getAmount());
        payment.markCompleted();
        System.out.println("Payment successful: " + payment.getId());

        // Free spot
        ParkingLevel level = levels.stream()
                .filter(l -> l.getId() == spot.getLevelID())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid level ID"));

        level.freeSpot(spot.getId(), spot.getParkingSpotType());

        // Complete ticket
        ticketService.completeTicket(ticketId);

        System.out.println("Vehicle exited: " + vehicle + " | Fee: $" + feeAmount);
    }

    public AvailabilityInfo getAvailability() {
        AvailabilityInfo info = new AvailabilityInfo();
        for (ParkingSpotType type : ParkingSpotType.values()) {
            int total = 0;
            for (ParkingLevel level : levels) {
                total += level.getAvailableCount(type);
            }
            info.setAvailable(type, total);
        }
        return info;
    }

    private Optional<ParkingSpot> findSpot(int spotId) {
        for (ParkingLevel level : levels) {
            Optional<ParkingSpot> spot = level.getSpot(spotId);
            if (spot.isPresent()) {
                return spot;
            }
        }
        return Optional.empty();
    }

    public static class AvailabilityInfo {
        private int twoWheeler;
        private int fourWheeler;
        private int eightWheeler;

        void setAvailable(ParkingSpotType type, int count) {
            switch (type) {
                case TWO_WHEELER -> twoWheeler = count;
                case FOUR_WHEELER -> fourWheeler = count;
                case EIGHT_WHEELER -> eightWheeler = count;
            }
        }

        @Override
        public String toString() {
            return String.format("Available - 2W: %d, 4W: %d, 8W: %d",
                    twoWheeler, fourWheeler, eightWheeler);
        }
    }
}
