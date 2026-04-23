package meetingRoom.repository;

import meetingRoom.exception.RoomNotFoundException;
import meetingRoom.model.MeetingRoom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomRepository {
    private final Map<String, MeetingRoom> rooms = new ConcurrentHashMap<>();  // {roomID, meetingRoom}

    public void save(MeetingRoom room) {
        rooms.put(room.getId(), room);
    }

    public MeetingRoom getRoomByID(String roomID) {
        MeetingRoom room = rooms.get(roomID);
        if (room == null) {
            throw new RoomNotFoundException(roomID);
        }

        return room;
    }

    public List<MeetingRoom> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }
}
