package meetingRoom.exception;

public class RoomNotAvailableException extends RuntimeException {
    public RoomNotAvailableException(String roomName, String timeSlot) {
        super("Room '" + roomName + "' is not available for slot: " + timeSlot);
    }
}
