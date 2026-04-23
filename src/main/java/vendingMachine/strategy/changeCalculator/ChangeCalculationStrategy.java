package vendingMachine.strategy.changeCalculator;

import vendingMachine.enums.Coin;

import java.util.Map;

public interface ChangeCalculationStrategy {
    Map<Coin, Integer> calculateChange(int changeAmount);
}
