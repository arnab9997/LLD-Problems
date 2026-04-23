package parkingLot.strategy.fees;

import parkingLot.enums.ParkingSpotType;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

public class HourlyFeesStrategy implements FeeCalculationStrategy {
    private static final Map<ParkingSpotType, Double> HOURLY_RATES = Map.of(
            ParkingSpotType.TWO_WHEELER, 10.0,
            ParkingSpotType.FOUR_WHEELER, 20.0,
            ParkingSpotType.EIGHT_WHEELER, 30.0
    );

    @Override
    public double calculateFees(OffsetDateTime entryTime, OffsetDateTime exitTime, ParkingSpotType parkingSpotType) {
        long totalHours = getTotalHours(entryTime, exitTime);
        return totalHours * HOURLY_RATES.get(parkingSpotType);
    }

    private long getTotalHours(OffsetDateTime entryTime, OffsetDateTime exitTime) {
        return Math.max(1, Duration.between(entryTime, exitTime).toHours());
    }
}
