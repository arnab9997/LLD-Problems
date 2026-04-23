package elevatorSystem.observer;

import elevatorSystem.enums.Direction;

public class ConsoleDisplay implements ElevatorObserver {

    @Override
    public void onFloorChanged(int elevatorId, int floor, Direction direction) {
        System.out.printf("[Console Display] Elevator %d | Floor: %d | %s \n", elevatorId, floor, direction);
    }
}
