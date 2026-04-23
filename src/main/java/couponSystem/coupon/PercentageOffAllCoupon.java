package couponSystem.coupon;

import couponSystem.LineItem;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor
public class PercentageOffAllCoupon implements Coupon {
    private final double discountPercent;

    @Override
    public void apply(List<LineItem> items) {
        for (LineItem item : items) {
            double multiplier = 1 - (discountPercent / 100.0);
            BigDecimal newPrice = item.getEffectivePrice().multiply(BigDecimal.valueOf(multiplier));
            item.setEffectivePrice(newPrice.setScale(2, RoundingMode.HALF_UP));
        }
    }
}