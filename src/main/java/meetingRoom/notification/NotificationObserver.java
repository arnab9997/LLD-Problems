package meetingRoom.notification;

import meetingRoom.model.Booking;

public interface NotificationObserver {
    void onBookingConfirmed(Booking booking);
    void onBookingCancelled(Booking booking);
}
