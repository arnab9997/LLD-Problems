package meetingRoom.strategy.filter;

import meetingRoom.model.MeetingRoom;

/**
 * Strategy interface for filtering rooms.
 * Compose multiple filters with AND semantics via chaining.
 * New criteria (e.g., floor, AV equipment grade) add a new impl — no existing code changes.
 */
public interface RoomFilter {
    boolean matches(MeetingRoom room);

    default RoomFilter and(RoomFilter other) {
        return room -> matches(room) && other.matches(room);
    }
}
