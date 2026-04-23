package parkingLot.strategy.fees;

import parkingLot.enums.ParkingSpotType;

import java.time.OffsetDateTime;
import java.util.Map;

public class FlatRateFeesStrategy implements FeeCalculationStrategy {
    private static final Map<ParkingSpotType, Double> VEHICLE_FEES_RATE = Map.of(
            ParkingSpotType.TWO_WHEELER, 10.0,
            ParkingSpotType.FOUR_WHEELER, 15.0,
            ParkingSpotType.EIGHT_WHEELER, 20.0);


    @Override
    public double calculateFees(OffsetDateTime entryTime, OffsetDateTime exitTime, ParkingSpotType parkingSpotType) {
        return VEHICLE_FEES_RATE.get(parkingSpotType);
    }
}
