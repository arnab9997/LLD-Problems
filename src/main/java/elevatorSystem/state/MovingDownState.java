package elevatorSystem.state;

import elevatorSystem.ElevatorCar;
import elevatorSystem.enums.Direction;

/**
 * Elevator is moving downward.
 * Serves all stops below current floor before reversing (SCAN).
 */
public class MovingDownState implements ElevatorState {

    @Override
    public void move(ElevatorCar car) {
        if (car.hasStopsBelow(car.getCurrentFloor())) {
            car.advanceFloorTo(car.getCurrentFloor() - 1);
            car.serveCurrentFloorIfStopped();
        } else {
            car.transitionTo(new IdleState());
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.DOWN;
    }
}
