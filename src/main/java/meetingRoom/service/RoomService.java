package meetingRoom.service;

import lombok.RequiredArgsConstructor;
import meetingRoom.model.MeetingRoom;
import meetingRoom.model.TimeSlot;
import meetingRoom.repository.BookingRepository;
import meetingRoom.repository.RoomRepository;
import meetingRoom.strategy.filter.RoomFilter;

import java.util.List;

@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public void addRoom(MeetingRoom room) {
        roomRepository.save(room);
    }

    public List<MeetingRoom> getAvailableRooms(TimeSlot slot, RoomFilter filter) {
        return roomRepository.getAllRooms().stream()
                .filter(filter::matches)
                .filter(room -> isAvailable(room.getId(), slot))
                .toList();

    }

    private boolean isAvailable(String roomId, TimeSlot slot) {
        return bookingRepository.findActiveConflicts(roomId, slot).isEmpty();
    }
}
