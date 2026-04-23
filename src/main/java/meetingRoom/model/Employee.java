package meetingRoom.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.ZoneId;

@Getter
@ToString
@RequiredArgsConstructor
public class Employee {
    private final String id;
    private final String name;
    private final String email;
    private final ZoneId timeZone;
}
