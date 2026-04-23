package vendingMachine;

import vendingMachine.strategy.changeCalculator.ChangeCalculationStrategy;
import vendingMachine.strategy.changeCalculator.GreedyChangeCalculation;
import vendingMachine.enums.Coin;
import vendingMachine.inventory.CoinInventory;
import vendingMachine.inventory.ProductInventory;
import vendingMachine.model.Product;

import java.util.Map;

public class VendingMachineDemo {

    public static void main(String[] args) {
        CoinInventory coinInventory = new CoinInventory();
        ProductInventory productInventory = new ProductInventory();
        ChangeCalculationStrategy changeStrategy = new GreedyChangeCalculation(coinInventory);
        VendingMachine machine = new VendingMachine(coinInventory, productInventory, changeStrategy);
        AdminConsole adminConsole = new AdminConsole(machine);

        // ── adminConsole setup ───────────────────────────────────────────────────
        // Note: Product(name, code, price, quantity) — price comes before quantity
        adminConsole.addProduct(new Product("Cola",   1, 15, 5));
        adminConsole.addProduct(new Product("Chips",  2, 10, 3));
        adminConsole.addProduct(new Product("Water",  3, 7,  2));
        adminConsole.refillCoins(Map.of(
                Coin.TEN,  20,
                Coin.FIVE, 20,
                Coin.TWO,  20,
                Coin.ONE,  20
        ));

        System.out.println("=== Scenario 1: Happy path with exact change ===");
        machine.startSession();
        machine.selectProduct(2);                        // Chips @ ₹10
        machine.insertCoins(Map.of(Coin.TEN, 1));        // Insert ₹10 exactly
        machine.dispenseProduct();

        System.out.println("\n=== Scenario 2: Happy path with overpayment (change returned) ===");
        machine.startSession();
        machine.selectProduct(1);                        // Cola @ ₹15
        machine.insertCoins(Map.of(Coin.TEN, 2));        // Insert ₹20, change = ₹5
        machine.dispenseProduct();

        System.out.println("\n=== Scenario 3: Incremental coin insertion ===");
        machine.startSession();
        machine.selectProduct(3);                        // Water @ ₹7
        machine.insertCoins(Map.of(Coin.TWO, 1));        // ₹2 — still need ₹5 more
        machine.insertCoins(Map.of(Coin.FIVE, 1));       // ₹5 — now ₹7 total, enough
        machine.dispenseProduct();

        System.out.println("\n=== Scenario 4: Cancellation mid-flow (coins refunded) ===");
        machine.startSession();
        machine.selectProduct(1);                        // Cola @ ₹15
        machine.insertCoins(Map.of(Coin.TEN, 1));        // ₹10 inserted
        machine.cancel();                                // Refund ₹10

        System.out.println("\n=== Scenario 5: Cancel during product selection (no coins) ===");
        machine.startSession();
        machine.cancel();                                // Just close session

        System.out.println("\nAll scenarios completed.");
    }
}