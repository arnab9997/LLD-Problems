package parkingLot.gate;

import parkingLot.ParkingLot;
import parkingLot.models.Vehicle;

import java.util.UUID;

public class ExitGate {
    private final int gateNumber;
    private final ParkingLot parkingLot;

    public ExitGate(int gateNumber, ParkingLot parkingLot) {
        this.gateNumber = gateNumber;
        this.parkingLot = parkingLot;
    }

    public void exit(UUID ticketId, Vehicle vehicle) {
        System.out.println("Exit Gate " + gateNumber + " processing: " + vehicle.getVehicleNumber());
        parkingLot.exitVehicle(ticketId, vehicle);
    }
}
