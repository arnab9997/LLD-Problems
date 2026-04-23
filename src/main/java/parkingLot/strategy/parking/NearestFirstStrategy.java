package parkingLot.strategy.parking;

import parkingLot.ParkingLevel;
import parkingLot.enums.ParkingSpotType;
import parkingLot.models.ParkingSpot;

import java.util.List;
import java.util.Optional;

public class NearestFirstStrategy implements ParkingStrategy {

    @Override
    public Optional<ParkingSpot> findAndAllocateSpot(List<ParkingLevel> levels, ParkingSpotType spotType) {
        // Try levels in order (level 1, 2, 3...)
        for (ParkingLevel level : levels) {
            Optional<ParkingSpot> spot = level.allocateSpot(spotType);
            if (spot.isPresent()) {
                return spot;
            }
        }
        return Optional.empty();
    }
}
