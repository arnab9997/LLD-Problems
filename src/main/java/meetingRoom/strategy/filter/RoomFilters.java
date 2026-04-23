package meetingRoom.strategy.filter;

import meetingRoom.enums.Feature;

import java.util.Set;

/**
 * Concrete RoomFilter implementations.
 * Each filter has exactly one criterion — compose with RoomFilter#and() for multi-criteria search.
 */
public final class RoomFilters {
    private RoomFilters() {}    // Utility class

    public static RoomFilter minCapacity(int requiredCapacity) {
        return room -> room.getCapacity() >= requiredCapacity;
    }

    public static RoomFilter hasFeatures(Set<Feature> requiredFeatures) {
        return room -> room.hasFeatures(requiredFeatures);
    }

    public static RoomFilter inLocation(String requiredLocation) {
        return room -> room.getLocation().equals(requiredLocation);
    }
}
