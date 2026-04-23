package elevatorSystem.observer;

import elevatorSystem.enums.Direction;

/**
 * Observer for elevator events.
 * Receives primitive snapshots (not the live ElevatorCar) to avoid concurrent visibility issues and tight coupling.
 */
public interface ElevatorObserver {
    void onFloorChanged(int elevatorId, int floor, Direction direction);
}
