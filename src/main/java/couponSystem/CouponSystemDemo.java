package couponSystem;

import couponSystem.coupon.NextItemPercentageOffCoupon;
import couponSystem.coupon.NthItemTypeDiscountCoupon;
import couponSystem.coupon.PercentageOffAllCoupon;
import couponSystem.enums.ProductType;

import java.math.BigDecimal;

public class CouponSystemDemo {
    public static void main(String[] args) {
        Product fan  = new Product("FAN",  BigDecimal.valueOf(1000), ProductType.ELECTRONICS);
        Product sofa = new Product("SOFA", BigDecimal.valueOf(2000), ProductType.FURNITURE);
        Product lamp = new Product("LAMP", BigDecimal.valueOf(500),  ProductType.DECORATIVE);

        ShoppingCart cart = new ShoppingCart();
        cart.addProduct(fan);
        cart.addProduct(sofa);
        cart.addProduct(lamp);

        CartPriceService service = new CartPriceService();
        service.addCoupon(new PercentageOffAllCoupon(10));
        service.addCoupon(new NthItemTypeDiscountCoupon(2, ProductType.FURNITURE, 20));
        service.addCoupon(new NextItemPercentageOffCoupon(ProductType.ELECTRONICS, 15));

        System.out.printf("Total before applying coupons: %s\n", cart.getCartTotal());
        System.out.printf("Total after applying coupons: %s\n", service.calculateTotal(cart));
    }
}
