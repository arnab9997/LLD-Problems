package couponSystem.coupon;

import couponSystem.LineItem;

import java.util.List;

public interface Coupon {
    void apply(List<LineItem> items);
}