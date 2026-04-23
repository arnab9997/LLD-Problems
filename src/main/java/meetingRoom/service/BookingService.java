package meetingRoom.service;

import lombok.RequiredArgsConstructor;
import meetingRoom.exception.RoomNotAvailableException;
import meetingRoom.model.Booking;
import meetingRoom.model.Employee;
import meetingRoom.model.MeetingRoom;
import meetingRoom.model.RecurrenceRule;
import meetingRoom.model.TimeSlot;
import meetingRoom.notification.NotificationObserver;
import meetingRoom.repository.BookingRepository;
import meetingRoom.repository.RoomRepository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Responsibilities (SRP):
 *   - Conflict detection
 *   - Booking lifecycle (create, cancel)
 *   - Recurrence expansion (delegates to RecurrenceRule)
 *   - Observer notification dispatch
 * Room search / filtering is intentionally in RoomSearchService.
 */

@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final List<NotificationObserver> observers;

    // Per-room lock map — lazily created, never removed
    private final ConcurrentHashMap<String, ReentrantLock> roomLocks = new ConcurrentHashMap<>();

    public Booking bookRoom(String roomID, Employee organizer, TimeSlot timeSlot, Set<Employee> participants) {
        MeetingRoom room = roomRepository.getRoomByID(roomID);
        ReentrantLock lock = getLockForRoom(roomID);
        lock.lock();
        try {
            assertNoConflict(roomID, timeSlot);
            Booking booking = new Booking(organizer, participants, timeSlot, room);
            bookingRepository.save(booking);
            notifyConfirmed(booking);
            return booking;
        } finally {
            lock.unlock();
        }
    }

    // Entire validate + persist is one atomic section under lock
    public List<Booking> bookRecurringMeeting(String roomID, Employee organizer, TimeSlot baseSlot, Set<Employee> participants, RecurrenceRule rule) {
        MeetingRoom room = roomRepository.getRoomByID(roomID);  // pure read, outside lock
        List<TimeSlot> occurrences = rule.expand(baseSlot);     // pure computation, outside lock

        ReentrantLock lock = getLockForRoom(roomID);
        lock.lock();
        try {
            // Validate ALL slots before persisting any — all-or-nothing, under lock
            for (TimeSlot slot : occurrences) {
                assertNoConflict(roomID, slot);
            }

            List<Booking> created = occurrences.stream()
                    .map(slot -> {
                        Booking b = new Booking(organizer, participants, slot, room);
                        bookingRepository.save(b);
                        return b;
                    }).toList();

            created.forEach(this::notifyConfirmed);
            return created;
        } finally {
            lock.unlock();
        }
    }

    public void cancelBooking(String bookingID) {
        Booking booking = bookingRepository.getByID(bookingID);
        ReentrantLock lock = getLockForRoom(booking.getMeetingRoom().getId());
        lock.lock();
        try {
            if (!booking.isBookingActive()) {
                return;  // Idempotent — already cancelled, nothing to do
            }

            booking.cancelBooking();
            notifyCancelled(booking);
        } finally {
            lock.unlock();
        }
    }

    private ReentrantLock getLockForRoom(String roomId) {
        return roomLocks.computeIfAbsent(roomId, id -> new ReentrantLock());
    }

    private void assertNoConflict(String roomID, TimeSlot timeSlot) {
        List<Booking> conflicts = bookingRepository.findActiveConflicts(roomID, timeSlot);
        if (!conflicts.isEmpty()) {
            throw new RoomNotAvailableException(roomID, timeSlot.toString());
        }
    }

    private void notifyConfirmed(Booking booking) {
        observers.forEach(o -> {
            try {
                o.onBookingConfirmed(booking);
            } catch (Exception e) {
                System.err.println("[WARN] Notification failed: " + e.getMessage());
            }
        });
    }

    private void notifyCancelled(Booking booking) {
        observers.forEach(o -> {
            try {
                o.onBookingCancelled(booking);
            } catch (Exception e) {
                System.err.println("[WARN] Notification failed: " + e.getMessage());
            }
        });
    }
}
