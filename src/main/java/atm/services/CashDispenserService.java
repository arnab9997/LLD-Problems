package atm.services;

import atm.chainOfResponsibility.DispenseChain;
import atm.chainOfResponsibility.NoteDispenser;
import atm.exception.ChangeNotAvailableException;

/**
 * Facade over the note-dispenser chain. Builds the default chain (100 → 50 → 20)
 * and exposes simple dispense/check APIs.
 *
 * Two layers of defence against partial dispense:
 *  - dispenseCash() pre-checks via canDispenseCash() before touching the chain — primary guard.
 *  - NoteDispenser.dispense() throws ChangeNotAvailableException if the chain runs dry
 *    mid-dispense — defensive fallback for callers that bypass this service directly.
 */
public class CashDispenserService {

    private static final int MIN_DISPENSABLE_DENOMINATION = 20;

    private final DispenseChain chain;

    public CashDispenserService(int notes100, int notes50, int notes20) {
        NoteDispenser d100 = new NoteDispenser(100, notes100);
        NoteDispenser d50  = new NoteDispenser(50,  notes50);
        NoteDispenser d20  = new NoteDispenser(20,  notes20);
        d100.setNextChain(d50);
        d50.setNextChain(d20);
        this.chain = d100;
    }

    /**
     * Returns true if the amount is a valid withdrawal denomination AND
     * the chain has sufficient notes to satisfy it.
     */
    public boolean canDispenseCash(int amount) {
        if (!isValidWithdrawalAmount(amount)) {
            return false;
        }
        return chain.canDispense(amount);
    }

    /**
     * Dispenses the requested amount from the note chain.
     * Pre-checks dispensability before touching the chain so that
     * no notes are dispensed if the full amount cannot be satisfied.
     *
     * @throws ChangeNotAvailableException if amount is invalid or chain
     *         has insufficient notes
     */
    public void dispenseCash(int amount) {
        if (!canDispenseCash(amount)) {
            throw new ChangeNotAvailableException(amount);
        }
        chain.dispense(amount); // safe — dispensability already validated above
    }

    public boolean isValidWithdrawalAmount(int amount) {
        return amount % MIN_DISPENSABLE_DENOMINATION == 0;
    }
}
