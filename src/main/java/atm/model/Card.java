package atm.model;

import atm.enums.CardStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Card {
    private final String cardNumber;
    private final String pin;
    private CardStatus status = CardStatus.ACTIVE;

    public void blockCard() {
        this.status = CardStatus.BLOCKED;
    }

    public boolean isCardBlocked() {
        return this.status == CardStatus.BLOCKED;
    }

    /**
     * PIN is a secret and must never leave this object.
     * BankService.authenticate() delegates here instead of pulling the PIN out.
     */
    public boolean verifyPin(String inputPin) {
        return this.pin.equals(inputPin);
    }
}
