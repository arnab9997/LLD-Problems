package vendingMachine.strategy.changeCalculator;

import vendingMachine.inventory.CoinInventory;
import vendingMachine.enums.Coin;
import vendingMachine.exception.ChangeNotAvailableException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class GreedyChangeCalculation implements ChangeCalculationStrategy {
    private final CoinInventory coinInventory;
    private static final Coin[] COINS_DESCENDING;

    static {
        COINS_DESCENDING = Coin.values().clone();
        Arrays.sort(COINS_DESCENDING, Comparator.comparingInt(Coin::getValue).reversed());
    }

    public GreedyChangeCalculation(CoinInventory coinInventory) {
        this.coinInventory = coinInventory;
    }

    @Override
    public Map<Coin, Integer> calculateChange(int changeAmount) {
        Map<Coin, Integer> result = new HashMap<>();
        int remaining = changeAmount;

        for (Coin coin : COINS_DESCENDING) {
            int coinValue = coin.getValue();
            int coinsRequired = remaining / coinValue;

            if (coinsRequired > 0) {
                int availableCoins = coinInventory.getCount(coin);
                int coinsUsed = Math.min(coinsRequired, availableCoins);

                if (coinsUsed > 0) {
                    result.put(coin, coinsUsed);
                    remaining -= coinValue * coinsUsed;
                }
            }
        }

        if (remaining != 0) {
            throw new ChangeNotAvailableException(remaining);
        }

        return result;
    }
}
