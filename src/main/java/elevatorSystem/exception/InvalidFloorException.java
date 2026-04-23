package elevatorSystem.exception;

public class InvalidFloorException extends RuntimeException {
    public InvalidFloorException(int floor, int numFloors) {
        super("Floor " + floor + " doesn't exist. Available floors: [1, " + numFloors + "]");
    }
}
