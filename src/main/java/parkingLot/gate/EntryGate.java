package parkingLot.gate;

import parkingLot.ParkingLot;
import parkingLot.models.Ticket;
import parkingLot.models.Vehicle;

public class EntryGate {
    private final int gateNumber;
    private final ParkingLot parkingLot;

    public EntryGate(int gateNumber, ParkingLot parkingLot) {
        this.gateNumber = gateNumber;
        this.parkingLot = parkingLot;
    }

    public Ticket enter(Vehicle vehicle) {
        System.out.println("Entry Gate " + gateNumber + " processing: " + vehicle.getVehicleNumber());
        return parkingLot.parkVehicle(vehicle);
    }
}
