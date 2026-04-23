package meetingRoom.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String roomName) {
        super("No room found with name: " + roomName);
    }
}
