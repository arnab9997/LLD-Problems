package couponSystem.coupon;

import couponSystem.LineItem;
import couponSystem.enums.ProductType;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * D% off on Nth item of type T
 */
@RequiredArgsConstructor
public class NthItemTypeDiscountCoupon implements Coupon {
    private final int n;
    private final ProductType targetType;
    private final double discountPercent;

    @Override
    public void apply(List<LineItem> items) {
        int count = 0;
        for (LineItem item : items) {
            if (item.getProduct().getType() == targetType) {
                count++;
                if (count % n == 0) {
                    double multiplier = 1 - (discountPercent / 100.0);
                    BigDecimal newPrice = item.getEffectivePrice().multiply(BigDecimal.valueOf(multiplier));
                    item.setEffectivePrice(newPrice.setScale(2, RoundingMode.HALF_UP));
                }
            }
        }
    }
}
