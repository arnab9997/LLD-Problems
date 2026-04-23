package vendingMachine;

import vendingMachine.enums.Coin;
import vendingMachine.model.Product;

import java.util.Map;

public class AdminConsole {
    private final VendingMachine machine;

    public AdminConsole(VendingMachine machine) {
        this.machine = machine;
    }

    public void addProduct(Product product) {
        machine.addProduct(product);
    }

    public void restockProduct(int productCode, int quantity) {
        machine.restockProduct(productCode, quantity);
    }

    public void refillCoins(Map<Coin, Integer> coins) {
        machine.refillCoins(coins);
    }
}
