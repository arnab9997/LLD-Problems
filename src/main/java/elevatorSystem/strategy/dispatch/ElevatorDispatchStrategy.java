package elevatorSystem.strategy.dispatch;

import elevatorSystem.ElevatorCar;
import elevatorSystem.Request;

import java.util.List;
import java.util.Optional;

/**
 * Strategy for dispatching an EXTERNAL request to the best elevator.
 * Swap implementations without touching ElevatorCar or ElevatorSystem.
 */
public interface ElevatorDispatchStrategy {
    Optional<ElevatorCar> select(List<ElevatorCar> elevators, Request request);
}
