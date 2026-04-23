package elevatorSystem;

import elevatorSystem.enums.Direction;
import elevatorSystem.enums.RequestSource;
import elevatorSystem.exception.ElevatorNotFoundException;
import elevatorSystem.exception.InvalidFloorException;
import elevatorSystem.observer.ConsoleDisplay;
import elevatorSystem.observer.ElevatorObserver;
import elevatorSystem.strategy.dispatch.ElevatorDispatchStrategy;
import elevatorSystem.strategy.dispatch.NearestElevatorDispatchStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Facade over the elevator system.
 *
 * Responsibilities: lifecycle (start/shutdown), external hall calls, internal cabin calls.
 *
 * Assumptions:
 *  - Elevator count and floor count are fixed at construction.
 *  - All elevators serve all floors.
 *  - If no suitable elevator is found for an external request, the request
 *    is dropped with a log. (Production would queue and retry — out of scope.)
 */
public class ElevatorSystem {
    private final List<ElevatorCar> elevators;
    private final ElevatorDispatchStrategy selectionStrategy;
    private final ExecutorService executor;

    public ElevatorSystem(int numElevators, int numFloors) {
        this.selectionStrategy = new NearestElevatorDispatchStrategy();
        this.executor = Executors.newFixedThreadPool(numElevators);

        ElevatorObserver display = new ConsoleDisplay();
        List<ElevatorCar> list = new ArrayList<>();
        for (int i = 1; i <= numElevators; i++) {
            ElevatorCar car = new ElevatorCar(i, numFloors);
            car.addObserver(display);
            list.add(car);
        }
        this.elevators = Collections.unmodifiableList(list);
    }

    public void start() {
        elevators.forEach(executor::submit);
        System.out.println("System started - " + elevators.size() + " elevator(s).\n");
    }

    public void requestElevator(int floor, Direction direction) {
        System.out.printf(">> EXTERNAL: floor %d [%s] \n", floor, direction);
        Request request = new Request(floor, direction);
        Optional<ElevatorCar> chosen = selectionStrategy.select(elevators, request);
        if (chosen.isPresent()) {
            chosen.get().addRequest(request);
        } else {
            System.out.println("   No suitable elevator — request dropped.");
        }
    }

    public void selectFloor(int elevatorId, int floor) {
        System.out.printf(">> INTERNAL: elevator %d → floor %d \n", elevatorId, floor);
        ElevatorCar car = findById(elevatorId);
        car.addRequest(new Request(floor, Direction.IDLE));
    }

    public void shutdown() {
        System.out.println("\nShutting down...");
        elevators.forEach(ElevatorCar::stop);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS))
                executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private ElevatorCar findById(int id) {
        return elevators.stream()
                .filter(e -> e.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ElevatorNotFoundException(id));
    }
}
