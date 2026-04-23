package elevatorSystem;

import elevatorSystem.enums.Direction;
import elevatorSystem.exception.ElevatorNotFoundException;
import elevatorSystem.exception.InvalidFloorException;

/**
 * Demonstrates the elevator system end-to-end.
 *
 * Scenario:
 *  E1 and E2 both start idle at floor 1.
 *  1. External: someone on floor 5 wants to go UP   → dispatched to nearest idle (E1).
 *  2. Internal: passenger in E1 presses floor 10    → E1 adds 10 to upRequests.
 *  3. External: someone on floor 3 wants to go DOWN → dispatched to E2 (idle, nearest).
 *  4. Internal: passenger in E2 presses floor 1     → E2 serves floor 3 then floor 1.
 */
public class ElevatorSystemDemo {

    public static void main(String[] args) throws InterruptedException {
        ElevatorSystem system = new ElevatorSystem(2, 20);
        system.start();

        system.requestElevator(5, Direction.UP);
        Thread.sleep(100);

        system.selectFloor(1, 10);
        Thread.sleep(100);

        system.requestElevator(3, Direction.DOWN);
        Thread.sleep(100);

        system.selectFloor(2, 1);

        try {
            system.selectFloor(99, 5);
        } catch (ElevatorNotFoundException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        try {
            system.requestElevator(99, Direction.UP);
        } catch (InvalidFloorException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        Thread.sleep(7000);
        system.shutdown();
    }
}
