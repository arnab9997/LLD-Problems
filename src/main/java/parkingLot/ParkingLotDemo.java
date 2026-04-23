package parkingLot;

import parkingLot.enums.ParkingSpotStatus;
import parkingLot.enums.ParkingSpotType;
import parkingLot.enums.VehicleType;
import parkingLot.gate.EntryGate;
import parkingLot.gate.ExitGate;
import parkingLot.models.ParkingSpot;
import parkingLot.models.Ticket;
import parkingLot.models.Vehicle;
import parkingLot.strategy.fees.HourlyFeesStrategy;
import parkingLot.strategy.parking.NearestFirstStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Demo showing concurrent parking operations.
 *
 * Tests:
 * - Multi-level parking
 * - Concurrent entry/exit
 * - No race conditions
 * - Idempotent exit
 */
public class ParkingLotDemo {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("=== Parking Lot System Demo ===\n");

        // Build parking lot (no builder pattern)
        ParkingLevel level1 = new ParkingLevel(1);
        level1.addSpot(new ParkingSpot(1, 1, ParkingSpotType.TWO_WHEELER, ParkingSpotStatus.UNOCCUPIED));
        level1.addSpot(new ParkingSpot(2, 1, ParkingSpotType.FOUR_WHEELER, ParkingSpotStatus.UNOCCUPIED));
        level1.addSpot(new ParkingSpot(3, 1, ParkingSpotType.FOUR_WHEELER, ParkingSpotStatus.UNOCCUPIED));
        level1.addSpot(new ParkingSpot(4, 1, ParkingSpotType.TWO_WHEELER, ParkingSpotStatus.UNOCCUPIED));

        ParkingLevel level2 = new ParkingLevel(2);
        level2.addSpot(new ParkingSpot(5, 2, ParkingSpotType.FOUR_WHEELER, ParkingSpotStatus.UNOCCUPIED));
        level2.addSpot(new ParkingSpot(6, 2, ParkingSpotType.EIGHT_WHEELER, ParkingSpotStatus.UNOCCUPIED));

        ParkingLot parkingLot = new ParkingLot(
                List.of(level1, level2),
                new NearestFirstStrategy(),
                new HourlyFeesStrategy()
        );

        // Create gates
        EntryGate entryGate1 = new EntryGate(1, parkingLot);
        EntryGate entryGate2 = new EntryGate(2, parkingLot);
        ExitGate exitGate1 = new ExitGate(1, parkingLot);

        // Show initial availability
        System.out.println("Initial: " + parkingLot.getAvailability());
        System.out.println();

        // Create vehicles using enum factory
        Vehicle car1 = VehicleType.FOUR_WHEELER.createVehicle("KA01A1");
        Vehicle car2 = VehicleType.FOUR_WHEELER.createVehicle("KA02B2");
        Vehicle bike1 = VehicleType.TWO_WHEELER.createVehicle("KA03C3");
        Vehicle truck1 = VehicleType.EIGHT_WHEELER.createVehicle("KA04D4");

        // Test concurrent entry
        System.out.println("--- Concurrent Entry ---");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Ticket>> entryFutures = new ArrayList<>();

        entryFutures.add(executor.submit(() -> entryGate1.enter(car1)));
        entryFutures.add(executor.submit(() -> entryGate2.enter(car2)));
        entryFutures.add(executor.submit(() -> entryGate1.enter(bike1)));
        entryFutures.add(executor.submit(() -> entryGate2.enter(truck1)));

        List<Ticket> tickets = new ArrayList<>();
        for (Future<Ticket> future : entryFutures) {
            tickets.add(future.get());
        }

        System.out.println("\nAll parked! " + parkingLot.getAvailability());
        System.out.println();

        Thread.sleep(1000);

        // Test exit
        System.out.println("--- Testing Exit ---");
        exitGate1.exit(tickets.get(0).getId(), car1);
        System.out.println("After exit: " + parkingLot.getAvailability());
        System.out.println();

        // Test idempotent exit
        System.out.println("--- Idempotent Exit ---");
        try {
            exitGate1.exit(tickets.get(0).getId(), car1);
        } catch (Exception e) {
            System.out.println("Prevented: " + e.getMessage());
        }
        System.out.println();

        // Test concurrent exit
        System.out.println("--- Concurrent Exit ---");
        List<Future<Void>> exitFutures = new ArrayList<>();
        exitFutures.add(executor.submit(() -> {
            exitGate1.exit(tickets.get(1).getId(), car2);
            return null;
        }));
        exitFutures.add(executor.submit(() -> {
            exitGate1.exit(tickets.get(2).getId(), bike1);
            return null;
        }));

        for (Future<Void> future : exitFutures) {
            future.get();
        }

        System.out.println("\nFinal: " + parkingLot.getAvailability());

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\n=== Demo Complete ===");
    }
}
