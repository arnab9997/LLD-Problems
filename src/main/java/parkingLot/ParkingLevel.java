package parkingLot;

import lombok.Getter;
import parkingLot.enums.ParkingSpotType;
import parkingLot.models.ParkingSpot;
import parkingLot.service.ParkingSpotService;

import java.util.Optional;

@Getter
public class ParkingLevel {
    private final int id;
    private final ParkingSpotService parkingSpotService;

    public ParkingLevel(int id) {
        this.id = id;
        this.parkingSpotService = new ParkingSpotService();
    }

    public void addSpot(ParkingSpot spot) {
        if (spot.getLevelID() != this.id) {
            throw new IllegalArgumentException("Spot belongs to different level");
        }
        parkingSpotService.registerSpot(spot);
    }

    public Optional<ParkingSpot> allocateSpot(ParkingSpotType spotType) {
        return parkingSpotService.allocateSpot(spotType);
    }

    public void freeSpot(int spotId, ParkingSpotType spotType) {
        parkingSpotService.freeSpot(spotId, spotType);
    }

    public int getAvailableCount(ParkingSpotType spotType) {
        return parkingSpotService.getAvailableCount(spotType);
    }

    public Optional<ParkingSpot> getSpot(int spotID) {
        return parkingSpotService.getSpot(spotID);
    }
}
