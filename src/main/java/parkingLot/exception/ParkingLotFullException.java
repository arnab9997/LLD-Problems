package parkingLot.exception;

public class ParkingLotFullException extends RuntimeException {
    public ParkingLotFullException(String vehicleType) {
        super("No available parking spots for vehicle type: " + vehicleType);
    }
}
