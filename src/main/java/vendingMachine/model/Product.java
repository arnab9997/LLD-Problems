package vendingMachine.model;

import lombok.Getter;
import lombok.ToString;
import vendingMachine.exception.OutOfStockException;

@Getter
@ToString
public class Product {
    private final String name;
    private final int code;
    private final int price;
    private int quantity;

    public Product(String name, int code, int price, int quantity) {
        this.name = name;
        this.code = code;
        this.quantity = quantity;
        this.price = price;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void reduceQuantity() {
        if (quantity <= 0) {
            throw new OutOfStockException(name);
        }
        this.quantity--;
    }
}
