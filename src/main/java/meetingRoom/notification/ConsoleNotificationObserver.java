package meetingRoom.notification;

import meetingRoom.model.Booking;


public class ConsoleNotificationObserver implements NotificationObserver {

    @Override
    public void onBookingConfirmed(Booking booking) {
        System.out.printf("[NOTIFICATION] Booking CONFIRMED | Room: %s | Slot: %s | Organizer: %s\n",
                booking.getMeetingRoom().getName(),
                booking.getTimeSlot(),
                booking.getOrganizer().getName());
        booking.getParticipants().forEach(p -> System.out.printf("Invite sent to %s", p.getName()));
    }

    @Override
    public void onBookingCancelled(Booking booking) {
        System.out.printf("[NOTIFICATION] Booking CANCELLED | Room: %s | Slot: %s | Organizer: %s\n",
                booking.getMeetingRoom().getName(),
                booking.getTimeSlot(),
                booking.getOrganizer().getName());
        booking.getParticipants().forEach(p -> System.out.printf("Cancellation sent to %s\n", p.getName()));
    }
}
