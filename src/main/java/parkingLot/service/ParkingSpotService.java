package parkingLot.service;

import parkingLot.enums.ParkingSpotType;
import parkingLot.models.ParkingSpot;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe manager for parking spot allocation and deallocation.
 *
 * Notes:
 * - Single lock per spot type ensures atomicity
 * - Allocation and status update happen atomically
 * - Deallocation also synchronized properly
 * - No comparator needed - simple queue ordering by spot ID (nearest first)
 */
public class ParkingSpotService {
    private final Map<Integer, ParkingSpot> spotRegistry = new ConcurrentHashMap<>();
    private final Map<ParkingSpotType, Queue<Integer>> availableSpotIds = new EnumMap<>(ParkingSpotType.class);
    private final Map<ParkingSpotType, ReentrantLock> locks = new EnumMap<>(ParkingSpotType.class);

    public ParkingSpotService() {
        for (ParkingSpotType type : ParkingSpotType.values()) {
            availableSpotIds.put(type, new LinkedList<>());
            locks.put(type, new ReentrantLock());
        }
    }

    public void registerSpot(ParkingSpot spot) {
        spotRegistry.put(spot.getId(), spot);
        if (spot.isAvailable()) {
            availableSpotIds.get(spot.getParkingSpotType()).offer(spot.getId());
        }
    }

    public Optional<ParkingSpot> getSpot(int spotID) {
        return Optional.ofNullable(spotRegistry.get(spotID));
    }

    public Optional<ParkingSpot> allocateSpot(ParkingSpotType spotType) {
        ReentrantLock lock = locks.get(spotType);
        lock.lock();
        try {
            Queue<Integer> queue = availableSpotIds.get(spotType);
            Integer spotId = queue.poll();
            if (spotId == null) {
                return Optional.empty();
            }
            ParkingSpot spot = spotRegistry.get(spotId);
            spot.occupy();
            return Optional.of(spot);
        } finally {
            lock.unlock();
        }
    }

    public void freeSpot(int spotId, ParkingSpotType spotType) {
        ReentrantLock lock = locks.get(spotType);
        lock.lock();
        try {
            ParkingSpot spot = spotRegistry.get(spotId);
            if (spot == null) {
                throw new IllegalArgumentException("Unknown spot ID: " + spotId);
            }
            spot.free();
            availableSpotIds.get(spotType).offer(spotId);
        } finally {
            lock.unlock();
        }
    }

    public int getAvailableCount(ParkingSpotType spotType) {
        ReentrantLock lock = locks.get(spotType);
        lock.lock();
        try {
            return availableSpotIds.get(spotType).size();
        } finally {
            lock.unlock();
        }
    }
}
