package vendingMachine.enums;

import lombok.Getter;

@Getter
public enum Coin {
    TEN(10),
    FIVE(5),
    TWO(2),
    ONE(1);

    private final int value;

    Coin(int value) {
        this.value = value;
    }
}
