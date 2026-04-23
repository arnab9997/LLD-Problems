package parkingLot.strategy.parking;

import parkingLot.ParkingLevel;
import parkingLot.enums.ParkingSpotType;
import parkingLot.models.ParkingSpot;

import java.util.List;
import java.util.Optional;

public class BestFitStrategy implements ParkingStrategy {

    @Override
    public Optional<ParkingSpot> findAndAllocateSpot(List<ParkingLevel> levels, ParkingSpotType spotType) {
        // Try to find exact matching spot type across all levels
        for (ParkingLevel level : levels) {
            Optional<ParkingSpot> spot = level.allocateSpot(spotType);
            if (spot.isPresent()) {
                return spot;
            }
        }

        // No matching spot found
        return Optional.empty();
    }
}
