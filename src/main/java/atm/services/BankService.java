package atm.services;

import atm.exception.AccountNotFoundException;
import atm.exception.CardNotLinkedException;
import atm.model.Account;
import atm.model.Card;

import java.util.HashMap;
import java.util.Map;

/**
 * Owns all bank-side data: accounts, cards, and their linkage.
 * Single point of truth for authentication, balance operations, and card lifecycle.
 */
public class BankService {

    private final Map<String, Account> accounts = new HashMap<>();
    private final Map<String, Card> cards = new HashMap<>();
    private final Map<String, Account> cardNumberAccountMap = new HashMap<>();

    public void createAccount(String accountNumber, double openingBalance) {
        accounts.put(accountNumber, new Account(accountNumber, openingBalance));
    }

    public void createCardAndLinkToAccount(String cardNumber, String pin, String accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        Card card = new Card(cardNumber, pin);
        cards.put(cardNumber, card);
        cardNumberAccountMap.put(cardNumber, account);
    }

    // ============================ ATM Operations ============================

    public Card findCard(String cardNumber) {
        return cards.get(cardNumber);
    }

    /**
     * FIX 1: Delegates to card.verifyPin() — raw PIN is never extracted from Card.
     * BankService doesn't need to know how PIN comparison works internally.
     */
    public boolean authenticate(Card card, String pin) {
        return card.verifyPin(pin);
    }

    public void blockCard(Card card) {
        card.blockCard();
        System.out.println("Card permanently blocked at bank level: " + card.getCardNumber());
    }

    public double getBalance(Card card) {
        return fetchAccountForCard(card).getBalance();
    }

    public void creditAmount(Card card, double amount) {
        fetchAccountForCard(card).creditAmount(amount);
    }

    public void debitAmount(Card card, double amount) {
        fetchAccountForCard(card).debitAmount(amount);
    }

    private Account fetchAccountForCard(Card card) {
        Account account = cardNumberAccountMap.get(card.getCardNumber());
        if (account == null) {
            throw new CardNotLinkedException(card.getCardNumber());
        }
        return account;
    }
}
