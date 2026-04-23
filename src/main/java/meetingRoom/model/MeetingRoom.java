package meetingRoom.model;

import lombok.Getter;
import lombok.ToString;
import meetingRoom.enums.Feature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Getter
@ToString
public class MeetingRoom {
    private final String id;
    private final String name;
    private final int capacity;
    private final String location;
    private final Set<Feature> features;

    public MeetingRoom(String id, String name, int capacity, String location, Set<Feature> features) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.features = features == null ? Collections.emptySet() : Collections.unmodifiableSet(EnumSet.copyOf(features));
    }

    public boolean hasFeatures(Set<Feature> requiredFeatures) {
        return this.features.containsAll(requiredFeatures);
    }
}
