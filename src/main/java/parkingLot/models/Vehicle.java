package parkingLot.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import parkingLot.enums.VehicleType;

@Getter
@ToString
@AllArgsConstructor
public class Vehicle {
    private final String vehicleNumber;
    private final VehicleType vehicleType;
}
