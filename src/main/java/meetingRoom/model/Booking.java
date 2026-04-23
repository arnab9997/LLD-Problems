package meetingRoom.model;

import lombok.Getter;
import lombok.ToString;
import meetingRoom.enums.BookingStatus;

import java.util.Set;
import java.util.UUID;

@Getter
@ToString
public class Booking {
    private final String id;
    private final Employee organizer;
    private final Set<Employee> participants;
    private final TimeSlot timeSlot;
    private final MeetingRoom meetingRoom;
    private volatile BookingStatus status;      // Status needs to be volatile

    public Booking(Employee organizer, Set<Employee> participants, TimeSlot timeSlot, MeetingRoom meetingRoom) {
        this.organizer = organizer;
        this.participants = participants;
        this.timeSlot = timeSlot;
        this.meetingRoom = meetingRoom;
        this.id = UUID.randomUUID().toString();
        status = BookingStatus.CONFIRMED;
    }

    public boolean isBookingActive() {
        return BookingStatus.CONFIRMED.equals(status);
    }

    public void cancelBooking() {
        this.status = BookingStatus.CANCELLED;
    }


}
