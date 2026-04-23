package vendingMachine.inventory;

import vendingMachine.enums.Coin;

import java.util.EnumMap;
import java.util.Map;

public class CoinInventory {
    private final Map<Coin, Integer> coins;

    public CoinInventory() {
        this.coins = new EnumMap<>(Coin.class);
        for (Coin c : Coin.values()) {
            coins.put(c, 0);
        }
    }

    public void addCoin(Coin coin) {
        coins.put(coin, coins.get(coin) + 1);
    }

    public void addCoins(Map<Coin, Integer> coins) {
        coins.forEach((c, v) -> this.coins.put(c, this.coins.get(c) + v));
    }

    /**
     * Caller is responsible for ensuring sufficient stock exists
     */
    public void removeCoins(Map<Coin, Integer> coins) {
        coins.forEach((c, v) -> this.coins.put(c, this.coins.get(c) - v));
    }

    public int getCount(Coin coin) {
        return this.coins.get(coin);
    }
}
