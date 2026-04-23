package elevatorSystem.exception;

public class ElevatorNotFoundException extends RuntimeException {
    public ElevatorNotFoundException(int elevatorId) {
        super("No elevator found with ID: " + elevatorId);
    }
}
