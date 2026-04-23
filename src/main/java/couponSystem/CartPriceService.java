package couponSystem;

import couponSystem.coupon.Coupon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CartPriceService {
    private final List<Coupon> coupons = new ArrayList<>();

    public void addCoupon(Coupon coupon) {
        coupons.add(coupon);
    }

    public BigDecimal calculateTotal(ShoppingCart cart) {
        List<LineItem> items = cart.getProducts().stream()
                .map(LineItem::new)
                .collect(Collectors.toList());

        for (Coupon coupon : coupons) {
            coupon.apply(items);
        }

        return items.stream()
                .map(LineItem::getEffectivePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
