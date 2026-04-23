package parkingLot.strategy.fees;

import parkingLot.enums.ParkingSpotType;

import java.time.OffsetDateTime;

public interface FeeCalculationStrategy {
    double calculateFees(OffsetDateTime entryTime, OffsetDateTime exitTime, ParkingSpotType parkingSpotType);
}
