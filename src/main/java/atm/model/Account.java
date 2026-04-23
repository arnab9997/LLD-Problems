package atm.model;

import atm.exception.InsufficientBalanceException;
import lombok.Getter;

import java.util.Map;

@Getter
public class Account {
    private final String accountNumber;
    private double balance;
    private Map<String, Card> issuedCards;

    public Account(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public void creditAmount(double amount) {
        this.balance += amount;
    }

    public void debitAmount(double amount) {
        if (balance < amount) {
            throw new InsufficientBalanceException();
        }
        balance -= amount;
    }
}
