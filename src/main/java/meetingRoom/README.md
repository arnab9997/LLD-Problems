## Functional Requirements
### User flow
* User selects:
    * A time slot
    * Participants
* System searches rooms based on
    * Availability of time slot
    * Room filters - capacity, features, location
* System returns list of available rooms
* User selects a room and confirms booking
* System creates the booking
* System sends booking confirmation notification
* User can also cancel a booked meeting
* User can book recurring meetings (daily/weekly/monthly)

### System flow
* Validate booking request
    * Time slot availability
    * Room availability
* Detect conflicts with existing bookings
* Persist meeting in repository
* Notify users upon:
    * Booking confirmation
    * Booking cancellation
* Update room availability

### Recurring booking flow:
* Expand recurrence rule into multiple time slots
* Validate conflicts for all occurrences
* If any conflicts occur -> reject the entire series
* If no conflicts -> persist all bookings

---

## Non-functional requirements
* Extensibility
    * New room filtering criteria should be easily pluggable
    * New notification channels can be added without modifying core logic
* Consistency
    * Booking creation must be atomic (no double bookings)
* Concurrency Safety
    * Multiple users may attempt to book the same room simultaneously
    * Per-room locking ensures atomic booking operations

---

## Core entities
* Employee
* Time Slot
* Meeting Room
* Recurrence Rule
* Booking
* Notification Observer
* Booking Service
* Room Service
* Room Filter

---

## Enums
* Booking Status
* Feature
* Recurrency Type

---

## State Models
N/A

---

## Design Patterns
* Observer Pattern - Meeting Room Scheduler notifies organizer & participants
* Strategy Pattern - Filter room for capacity, feature requirements, location

---

## API Design
* bookRoom(roomID, organizer, slot, participants)
* cancelBooking(bookingID)
* bookRecurringMeeting(roomID, organizer, baseSlot, participants, recurrenceRule)
* getAvailableRooms(slot, filter)

---

## DB Persistence
* Room
* Boooking

---

## Concurrency
* Concurrent booking requests for the same room are handled via per-room locks: ```Map<roomId, ReentrantLock>```
* Booking flow:
    * Acquire room lock
      → validate conflicts
      → persist booking
    * Release lock
* Benefits:
    * Prevents double booking
    * Allows parallel bookings across different rooms
    * Avoids global locking