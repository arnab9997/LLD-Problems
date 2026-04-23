package elevatorSystem;

import elevatorSystem.enums.Direction;
import elevatorSystem.enums.RequestSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Request {
    private final int targetFloor;
    private final Direction direction; // IDLE for internal cabin requests
}
