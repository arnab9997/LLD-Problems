package meetingRoom.repository;

import meetingRoom.exception.BookingNotFoundException;
import meetingRoom.model.Booking;
import meetingRoom.model.TimeSlot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookingRepository {
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();  // {bookingID -> Booking}

    public void save(Booking booking) {
        bookings.put(booking.getId(), booking);
    }

    public Booking getByID(String bookingID) {
        Booking booking = bookings.get(bookingID);
        if (booking == null) {
            throw new BookingNotFoundException(bookingID);
        }
        return booking;
    }

    /**
     * Returns all ACTIVE bookings for a given room that overlap the queried slot.
     */
    public List<Booking> findActiveConflicts(String roomID, TimeSlot slot) {
        return bookings.values().stream()
                .filter(b -> b.isBookingActive())
                .filter(b -> b.getMeetingRoom().getId().equals(roomID))
                .filter(b -> b.getTimeSlot().overlapsWith(slot))
                .toList();
    }
}
