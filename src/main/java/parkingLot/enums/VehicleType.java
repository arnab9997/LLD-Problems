package parkingLot.enums;

import lombok.Getter;
import parkingLot.models.Vehicle;

@Getter
public enum VehicleType {
    TWO_WHEELER(ParkingSpotType.TWO_WHEELER),
    FOUR_WHEELER(ParkingSpotType.FOUR_WHEELER),
    EIGHT_WHEELER(ParkingSpotType.EIGHT_WHEELER);

    private final ParkingSpotType requiredSpotType;

    VehicleType(ParkingSpotType requiredSpotType) {
        this.requiredSpotType = requiredSpotType;
    }

    public Vehicle createVehicle(String vehicleNumber) {
        return new Vehicle(vehicleNumber, this);
    }
}