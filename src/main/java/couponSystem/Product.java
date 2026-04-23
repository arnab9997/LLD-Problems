package couponSystem;

import couponSystem.enums.ProductType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class Product {
    private final String name;
    private final BigDecimal price;
    private final ProductType type;
}
