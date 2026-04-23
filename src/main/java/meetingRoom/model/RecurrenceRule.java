package meetingRoom.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import meetingRoom.enums.RecurrenceType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates all concrete TimeSlots between a base slot and an end date.
 */
@Getter
@RequiredArgsConstructor
public class RecurrenceRule {
    private final RecurrenceType type;
    private final LocalDate endDate;

    /**
     * Expands a base TimeSlot into all recurring occurrences up to endDate (inclusive).
     * The base slot itself is included as the first occurrence.
     */
    public List<TimeSlot> expand(TimeSlot base) {
        List<TimeSlot> slots = new ArrayList<>();
        TimeSlot current = base;

        while (!current.getStartTime().toLocalDate().isAfter(endDate)) {
            slots.add(current);
            current = shift(current);
        }

        return slots;
    }

    private TimeSlot shift(TimeSlot slot) {
        return switch (type) {
            case DAILY -> new TimeSlot(slot.getStartTime().plusDays(1),
                    slot.getEndTime().plusDays(1));
            case WEEKLY -> new TimeSlot(slot.getStartTime().plusWeeks(1),
                    slot.getEndTime().plusWeeks(1));
            case MONTHLY -> new TimeSlot(slot.getStartTime().plusMonths(1),
                    slot.getEndTime().plusMonths(1));
            default -> throw new IllegalArgumentException("Unknown recurrence type: " + type);
        };
    }
}
