package parkingLot.strategy.parking;

import parkingLot.ParkingLevel;
import parkingLot.enums.ParkingSpotType;
import parkingLot.models.ParkingSpot;

import java.util.List;
import java.util.Optional;

public interface ParkingStrategy {
    Optional<ParkingSpot> findAndAllocateSpot(List<ParkingLevel> levels, ParkingSpotType spotType);
}
