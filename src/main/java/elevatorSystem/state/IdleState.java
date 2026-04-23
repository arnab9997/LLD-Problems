package elevatorSystem.state;

import elevatorSystem.ElevatorCar;
import elevatorSystem.enums.Direction;

/**
 * Elevator is stopped with no pending work.
 * Picks a direction on the next cycle if a stop was registered.
 * Prefers UP; bias can be changed per requirements.
 */
public class IdleState implements ElevatorState {

    @Override
    public void move(ElevatorCar car) {
        int currentFloor = car.getCurrentFloor();
        if (car.hasStopsAbove(currentFloor)) {
            car.transitionTo(new MovingUpState());
        } else if (car.hasStopsBelow(currentFloor)) {
            car.transitionTo(new MovingDownState());
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.IDLE;
    }
}
