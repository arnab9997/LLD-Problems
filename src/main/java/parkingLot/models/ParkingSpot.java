package parkingLot.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import parkingLot.enums.ParkingSpotStatus;
import parkingLot.enums.ParkingSpotType;

@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class ParkingSpot {
    private final int id;
    private final int levelID;
    private final ParkingSpotType parkingSpotType;
    private ParkingSpotStatus parkingSpotStatus;

    public void occupy() {
        if (!ParkingSpotStatus.UNOCCUPIED.equals(parkingSpotStatus)) {
            throw new IllegalStateException("Cannot occupy spot in status: " + parkingSpotStatus);
        }

        this.parkingSpotStatus = ParkingSpotStatus.OCCUPIED;
    }

    public void free() {
        if (!ParkingSpotStatus.OCCUPIED.equals(parkingSpotStatus)) {
            throw new IllegalStateException("Cannot free spot in status: " + parkingSpotStatus);
        }

        this.parkingSpotStatus = ParkingSpotStatus.UNOCCUPIED;
    }

    public void markOutOfService() {
        this.parkingSpotStatus = ParkingSpotStatus.OUT_OF_SERVICE;
    }

    public boolean isAvailable() {
        return ParkingSpotStatus.UNOCCUPIED.equals(parkingSpotStatus);
    }
}
