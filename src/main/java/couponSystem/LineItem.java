package couponSystem;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A cart-scoped working copy of a {@link Product}.
 *
 * <p>Holds an {@code effectivePrice} that starts at the product's catalog price
 * and is progressively reduced as coupons are applied. The underlying
 * {@link Product} is never modified.
 *
 * <p>Example: a FAN priced at ₹1000 in the catalog becomes a LineItem
 * with effectivePrice ₹900 after a 10% coupon - the Product still reads ₹1000.
 */
@Getter
@Setter
public class LineItem {
    private final Product product;
    private BigDecimal effectivePrice;   // mutated as coupons apply

    public LineItem(Product product) {
        this.product = product;
        this.effectivePrice = product.getPrice();
    }
}
