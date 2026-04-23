package meetingRoom.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String bookingID) {
        super("No booking found with id: " + bookingID);
    }

}
