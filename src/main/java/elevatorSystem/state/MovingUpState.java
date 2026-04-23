package elevatorSystem.state;

import elevatorSystem.ElevatorCar;
import elevatorSystem.enums.Direction;

/**
 * Elevator is moving upward.
 * Serves all stops above current floor before reversing (SCAN).
 */
public class MovingUpState implements ElevatorState {

    @Override
    public void move(ElevatorCar car) {
        if (car.hasStopsAbove(car.getCurrentFloor())) {
            car.advanceFloorTo(car.getCurrentFloor() + 1);
            car.serveCurrentFloorIfStopped();
        } else {
            car.transitionTo(new IdleState());
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.UP;
    }
}