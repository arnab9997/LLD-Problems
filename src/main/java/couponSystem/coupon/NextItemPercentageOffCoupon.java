package couponSystem.coupon;

import couponSystem.LineItem;
import couponSystem.enums.ProductType;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * P% off on next item
 */
@RequiredArgsConstructor
public class NextItemPercentageOffCoupon implements Coupon {
    private final ProductType triggerType;
    private final double discountPercent;

    @Override
    public void apply(List<LineItem> items) {
        for (int i = 0; i < items.size() - 1; i++) {
            if (items.get(i).getProduct().getType() == triggerType) {
                LineItem next = items.get(i + 1);
                double multiplier = 1 - (discountPercent / 100.0);
                BigDecimal newPrice = next.getEffectivePrice().multiply(BigDecimal.valueOf(multiplier));
                next.setEffectivePrice(newPrice.setScale(2, RoundingMode.HALF_UP));
                i++;
            }
        }
    }
}
