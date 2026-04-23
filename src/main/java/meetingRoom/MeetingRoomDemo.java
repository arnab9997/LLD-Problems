package meetingRoom;

import meetingRoom.enums.Feature;
import meetingRoom.enums.RecurrenceType;
import meetingRoom.exception.RoomNotAvailableException;
import meetingRoom.model.Booking;
import meetingRoom.model.Employee;
import meetingRoom.model.MeetingRoom;
import meetingRoom.model.RecurrenceRule;
import meetingRoom.model.TimeSlot;
import meetingRoom.notification.ConsoleNotificationObserver;
import meetingRoom.repository.BookingRepository;
import meetingRoom.repository.RoomRepository;
import meetingRoom.service.BookingService;
import meetingRoom.service.RoomService;
import meetingRoom.strategy.filter.RoomFilters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class MeetingRoomDemo {

    public static void main(String[] args) {

        // ---- Wire dependencies ----
        RoomRepository roomRepo    = new RoomRepository();
        BookingRepository bookingRepo = new BookingRepository();
        BookingService bookingService = new BookingService(bookingRepo, roomRepo,
                List.of(new ConsoleNotificationObserver()));
        RoomService roomService   = new RoomService(roomRepo, bookingRepo);

        // ---- Seed rooms ----
        MeetingRoom alpha = new MeetingRoom("R1", "Alpha", 10, "Floor-1",
                EnumSet.of(Feature.PROJECTOR, Feature.WHITEBOARD));
        MeetingRoom beta  = new MeetingRoom("R2", "Beta",   6, "Floor-2",
                EnumSet.of(Feature.VIDEO_CONFERENCING));
        MeetingRoom gamma = new MeetingRoom("R3", "Gamma", 20, "Floor-1",
                EnumSet.of(Feature.PROJECTOR, Feature.VIDEO_CONFERENCING, Feature.WHITEBOARD));

        roomService.addRoom(alpha);
        roomService.addRoom(beta);
        roomService.addRoom(gamma);

        // ---- Seed employees ----
        Employee alice = new Employee("E1", "Alice", "alice@corp.com", ZoneId.of("Asia/Kolkata"));
        Employee bob   = new Employee("E2", "Bob",   "bob@corp.com",   ZoneId.of("Asia/Kolkata"));
        Employee carol = new Employee("E3", "Carol", "carol@corp.com", ZoneId.of("America/New_York"));

        // ----------------------------------------------------------------
        // Scenario 1: Simple booking
        // ----------------------------------------------------------------
        sep("Scenario 1: Simple booking");
        TimeSlot slot1 = new TimeSlot(
                LocalDateTime.of(2025, 6, 10, 10, 0),
                LocalDateTime.of(2025, 6, 10, 11, 0));
        Booking b1 = bookingService.bookRoom("R1", alice, slot1, Set.of(bob));
        System.out.println("Created: " + b1);

        // ----------------------------------------------------------------
        // Scenario 2: Conflict detection — overlapping slot rejected
        // ----------------------------------------------------------------
        sep("Scenario 2: Conflict detection");
        TimeSlot overlapping = new TimeSlot(
                LocalDateTime.of(2025, 6, 10, 10, 30),
                LocalDateTime.of(2025, 6, 10, 11, 30));
        try {
            bookingService.bookRoom("R1", bob, overlapping, Set.of());
            System.out.println("ERROR — should have thrown!");
        } catch (RoomNotAvailableException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        // ----------------------------------------------------------------
        // Scenario 3: Adjacent slots on same room are allowed
        // ----------------------------------------------------------------
        sep("Scenario 3: Adjacent slots allowed");
        TimeSlot slot2 = new TimeSlot(
                LocalDateTime.of(2025, 6, 10, 11, 0),
                LocalDateTime.of(2025, 6, 10, 12, 0));
        Booking b2 = bookingService.bookRoom("R1", bob, slot2, Set.of(carol));
        System.out.println("Created: " + b2);

        // ----------------------------------------------------------------
        // Scenario 4: Cancellation + idempotent double-cancel
        // ----------------------------------------------------------------
        sep("Scenario 4: Cancellation");
        bookingService.cancelBooking(b1.getId());
        System.out.println("After cancel: " + b1.getStatus());
        bookingService.cancelBooking(b1.getId());   // idempotent
        System.out.println("Double-cancel (idempotent): " + b1.getStatus());

        // ----------------------------------------------------------------
        // Scenario 5: Cancelled slot can be re-booked
        // ----------------------------------------------------------------
        sep("Scenario 5: Re-book after cancel");
        Booking b3 = bookingService.bookRoom("R1", carol, slot1, Set.of(alice, bob));
        System.out.println("Re-booked: " + b3);

        // ----------------------------------------------------------------
        // Scenario 6: Recurring booking — weekly standup, 3 occurrences
        // ----------------------------------------------------------------
        sep("Scenario 6: Recurring booking (weekly, 3 occurrences)");
        TimeSlot standupBase = new TimeSlot(
                LocalDateTime.of(2025, 6, 11, 9, 0),
                LocalDateTime.of(2025, 6, 11, 9, 30));
        RecurrenceRule weekly = new RecurrenceRule(RecurrenceType.WEEKLY, LocalDate.of(2025, 6, 25));

        List<Booking> recurring = bookingService.bookRecurringMeeting(
                "R2", alice, standupBase, Set.of(bob), weekly);
        System.out.println("Created " + recurring.size() + " recurring bookings:");
        recurring.forEach(b -> System.out.println("  " + b.getTimeSlot()));

        // ----------------------------------------------------------------
        // Scenario 7: Recurring conflict — one clash rejects entire series
        // ----------------------------------------------------------------
        sep("Scenario 7: Recurring conflict (all-or-nothing)");
        TimeSlot clashWeek2 = new TimeSlot(
                LocalDateTime.of(2025, 6, 18, 9, 0),
                LocalDateTime.of(2025, 6, 18, 9, 30));
        bookingService.bookRoom("R3", carol, clashWeek2, Set.of());
        System.out.println("Pre-booked week-2 on Gamma to create clash");

        TimeSlot conflictBase = new TimeSlot(
                LocalDateTime.of(2025, 6, 11, 9, 0),
                LocalDateTime.of(2025, 6, 11, 9, 30));
        RecurrenceRule weeklyR3 = new RecurrenceRule(RecurrenceType.WEEKLY, LocalDate.of(2025, 6, 25));
        try {
            bookingService.bookRecurringMeeting("R3", bob, conflictBase, Set.of(), weeklyR3);
            System.out.println("ERROR — should have thrown!");
        } catch (RoomNotAvailableException e) {
            System.out.println("Correctly rejected entire series: " + e.getMessage());
        }

        // ----------------------------------------------------------------
        // Scenario 8: Room search with composed filters
        // ----------------------------------------------------------------
        sep("Scenario 8: Search available rooms (cap>=8, projector, Floor-1)");
        TimeSlot searchSlot = new TimeSlot(
                LocalDateTime.of(2025, 6, 12, 14, 0),
                LocalDateTime.of(2025, 6, 12, 15, 0));
        var filter = RoomFilters.minCapacity(8)
                .and(RoomFilters.hasFeatures(EnumSet.of(Feature.PROJECTOR)))
                .and(RoomFilters.inLocation("Floor-1"));
        List<MeetingRoom> available = roomService.getAvailableRooms(searchSlot, filter);
        System.out.println("Available rooms: " + available);

        // ----------------------------------------------------------------
        // Scenario 9: Invalid TimeSlot rejected at construction
        // ----------------------------------------------------------------
        sep("Scenario 9: Invalid TimeSlot (end before start)");
        try {
            new TimeSlot(
                    LocalDateTime.of(2025, 6, 10, 11, 0),
                    LocalDateTime.of(2025, 6, 10, 10, 0));
        } catch (Exception e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        sep("Demo complete");
    }

    private static void sep(String title) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  " + title);
        System.out.println("=".repeat(60));
    }
}