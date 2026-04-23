package elevatorSystem;

import elevatorSystem.enums.Direction;
import elevatorSystem.exception.InvalidFloorException;
import elevatorSystem.observer.ElevatorObserver;
import elevatorSystem.state.ElevatorState;
import elevatorSystem.state.IdleState;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * A single elevator car.
 *
 * ── Scheduling ───────────────────────────────────────────────────────────────
 * SCAN algorithm via a boolean[] stops array (indexed by floor, 1-based).
 * stops[f] = true means "open doors at floor f on the way through".
 * Dedup is free: setting stops[f] = true twice is idempotent.
 * Next stop found by scanning in the current direction — O(numFloors),
 * negligible for any real building.
 *
 * ── State pattern ────────────────────────────────────────────────────────────
 * move() behaviour differs meaningfully per state:
 *   IdleState       — scans for any pending stop, picks a direction.
 *   MovingUpState   — advances one floor upward, serves stop if reached,
 *                     transitions to Idle when no stops remain above.
 *   MovingDownState — symmetric.
 * addRequest() is NOT on ElevatorState: with boolean[], registering a stop
 * is always stops[floor] = true regardless of state — no per-state logic needed.
 *
 * ── Concurrency model ────────────────────────────────────────────────────────
 * Two threads share this object:
 *   - Run loop (elevator's own thread) → calls advanceOneFloor() every 500 ms
 *   - ElevatorSystem thread            → calls addRequest()
 *
 * Both are synchronized on `this`. Shared mutable fields: currentFloor,
 * state, stops[].
 *
 * Observer callbacks fire OUTSIDE the lock: primitives are snapshotted before
 * notifying so observers cannot deadlock by calling back into synchronized
 * methods here.
 */
@Getter
public class ElevatorCar implements Runnable {
    private final int id;
    private final int topFloor;
    private int currentFloor;       // guarded by this
    private ElevatorState state;    // guarded by this
    final boolean[] stops;          // guarded by this
    private final List<ElevatorObserver> observers;
    private volatile boolean running;

    public ElevatorCar(int id, int topFloor) {
        this.id = id;
        this.topFloor = topFloor;
        this.currentFloor = 1;
        this.state = new IdleState();
        this.stops = new boolean[topFloor + 1]; // 1-indexed; [0] unused
        this.observers = new ArrayList<>();
        this.running = true;
    }

    public void addObserver(ElevatorObserver observer) {
        observers.add(observer);
    }

    public synchronized void addRequest(Request request) {
        int targetFloor = request.getTargetFloor();
        if (targetFloor < 1 || targetFloor > topFloor) {
            throw new InvalidFloorException(targetFloor, topFloor);
        }
        stops[targetFloor] = true;
    }

    public boolean hasStopsAbove(int floor) {
        for (int f = floor + 1; f <= topFloor; f++) {
            if (stops[f]) return true;
        }
        return false;
    }

    public boolean hasStopsBelow(int floor) {
        for (int f = floor - 1; f >= 1; f--) {
            if (stops[f]) return true;
        }
        return false;
    }

    public void serveCurrentFloorIfStopped() {
        if (stops[currentFloor]) {
            stops[currentFloor] = false;
            System.out.printf("Elevator %d ✓ stopped at floor %d \n", id, currentFloor);
        }
    }

    public void advanceFloorTo(int floor) {
        currentFloor = floor;
        notifyFloorChanged(currentFloor, state.getDirection());
    }

    public void transitionTo(ElevatorState newState) {
        this.state = newState;
    }

    @Override
    public void run() {
        while (running) {
            advanceOneFloor();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    private synchronized void advanceOneFloor() {
        state.move(this);
    }

    public synchronized int getCurrentFloor() {
        return currentFloor;
    }

    public synchronized Direction getDirection() {
        return state.getDirection();
    }

    public void stop() {
        running = false;
    }

    private void notifyFloorChanged(int floor, Direction dir) {
        for (ElevatorObserver o : observers) {
            o.onFloorChanged(id, floor, dir);
        }
    }
}