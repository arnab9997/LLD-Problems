package atm;

import atm.enums.OperationType;
import atm.model.Card;
import atm.services.BankService;
import atm.services.CashDispenserService;
import atm.state.ATMState;
import atm.state.IdleState;
import atm.state.SelectOperationState;
import lombok.Getter;
import lombok.Setter;


/**
 * ATMMachine holds shared ATM state and delegates every user action to the
 * current ATMState. It has NO instanceof checks and NO transition logic —
 * all transitions are owned by the state classes themselves.
 *
 * Think of ATMMachine as the "session": it keeps track of what card is
 * inserted, what PIN-authenticated account we're working with, and what
 * operation the user selected.
 */
@Getter
@Setter
public class ATMMachine {

    private final BankService bankService;
    private final CashDispenserService cashDispenserService;

    private ATMState currentState;
    private Card currentCard;
    private OperationType selectedOperation;

    public ATMMachine(BankService bankService, CashDispenserService cashDispenserService) {
        this.bankService = bankService;
        this.cashDispenserService = cashDispenserService;
        this.currentState = new IdleState();
    }

    // ============================ ATM Operations (delegated to current state) ============================

    public void insertCard(String cardNumber) {
        currentState.insertCard(this, cardNumber);
    }

    public void enterPin(String pin) {
        currentState.enterPin(this, pin);
    }

    public void selectOperation(OperationType op) {
        currentState.selectOperation(this, op);
    }

    public void performTransaction(int amount) {
        currentState.performTransaction(this, amount);
    }

    public void ejectCard() {
        currentState.ejectCard(this);
    }

    // ============================ State transitions (only called by states) ============================

    public void transitionTo(ATMState newState) {
        System.out.println("State: " + currentState.stateName() + " -> " + newState.stateName());
        this.currentState = newState;
    }

    /**
     * Called by TransactionState after completing (or failing) an operation.
     * ATMMachine decides what "back to menu" means - today it's SelectOperationState,
     * but if we ever insert a ReceiptPrintingState between transaction and menu,
     * only this method needs to change.
     */
    public void transitionToMenu() {
        transitionTo(new SelectOperationState());
    }

    public void resetSession() {
        System.out.println("Session ended. Card ejected.");
        this.currentCard = null;
        this.selectedOperation = null;
        this.currentState = new IdleState();
    }

    // ============================ Service accessors (used by states) ============================

    public Card findCard(String cardNumber) {
        return bankService.findCard(cardNumber);
    }

    public boolean authenticate(Card card, String pin) {
        return bankService.authenticate(card, pin);
    }

    public void blockCard() {
        bankService.blockCard(currentCard);
    }

    public double getBalance() {
        return bankService.getBalance(currentCard);
    }

    public void creditAmount(double amount) {
        bankService.creditAmount(currentCard, amount);
    }

    public void debitAmount(double amount) {
        bankService.debitAmount(currentCard, amount);
    }

    public boolean isValidWithdrawalAmount(int amount) {
        return cashDispenserService.isValidWithdrawalAmount(amount);
    }

    public void dispenseCash(int amount) {
        cashDispenserService.dispenseCash(amount);
    }

    public boolean canDispenseCash(int amount) {
        return cashDispenserService.canDispenseCash(amount);
    }
}