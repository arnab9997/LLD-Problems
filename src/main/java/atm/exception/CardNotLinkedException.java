package atm.exception;

public class CardNotLinkedException extends RuntimeException {
    public CardNotLinkedException(String cardNumber) {
        super("No account linked to card: " + cardNumber);
    }
}
