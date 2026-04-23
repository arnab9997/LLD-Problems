package meetingRoom.model;

import lombok.Getter;
import lombok.ToString;
import meetingRoom.exception.InvalidTimeSlotException;

import java.time.LocalDateTime;

@Getter
@ToString
public class TimeSlot {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public TimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isBefore(startTime)) {
            throw new InvalidTimeSlotException("endTime must be strictly after startTime");
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean overlapsWith(TimeSlot other) {
        return this.startTime.isBefore(other.endTime)
                && other.startTime.isBefore(this.endTime);
    }
}
