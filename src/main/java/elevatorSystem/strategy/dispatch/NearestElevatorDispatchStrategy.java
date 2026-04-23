package elevatorSystem.strategy.dispatch;

import elevatorSystem.ElevatorCar;
import elevatorSystem.Request;
import elevatorSystem.enums.Direction;

import java.util.List;
import java.util.Optional;

/**
 * Picks the closest idle elevator, or, one already heading the same way with the request floor still ahead of it.
 *
 * Suitability rules (mirrors SCAN):
 *  - IDLE  → always eligible
 *  - UP    → eligible only if request is UP and target >= currentFloor
 *  - DOWN  → eligible only if request is DOWN and target <= currentFloor
 */
public class NearestElevatorDispatchStrategy implements ElevatorDispatchStrategy {

    @Override
    public Optional<ElevatorCar> select(List<ElevatorCar> elevatorCars, Request request) {
        ElevatorCar best = null;
        int minDistance = Integer.MAX_VALUE;

        for (ElevatorCar car : elevatorCars) {
            if (isSuitable(car, request)) {
                int distance = Math.abs(car.getCurrentFloor() - request.getTargetFloor());
                if (distance < minDistance) {
                    minDistance = distance;
                    best = car;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    // NOTE: direction and floor are read in two separate synchronized calls — a stale
    // composite read is possible under contention. Worst case: suboptimal dispatch.
    // Not a correctness failure — addRequest() is still synchronized on the chosen car.
    private boolean isSuitable(ElevatorCar car, Request request) {
        return switch (car.getDirection()) {
            case IDLE -> true;
            case UP -> request.getDirection() == Direction.UP
                    && car.getCurrentFloor() <= request.getTargetFloor();
            case DOWN -> request.getDirection() == Direction.DOWN
                    && car.getCurrentFloor() >= request.getTargetFloor();
        };
    }
}
