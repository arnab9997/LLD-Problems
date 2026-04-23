package elevatorSystem.state;

import elevatorSystem.ElevatorCar;
import elevatorSystem.enums.Direction;

/**
 * Justification - move() logic is meaningfully different per state:
 *    IdleState picks a direction,
 *    MovingUpState scans upward and reverses when exhausted,
 *    MovingDownState is symmetric.
 */
public interface ElevatorState {
    void move(ElevatorCar elevatorCar);
    Direction getDirection();
}
