package atm.chainOfResponsibility;

import atm.exception.ChangeNotAvailableException;
import lombok.Getter;

/**
 * Handles dispensing for a single note denomination.
 * Construct with the desired denomination and available count.
 *
 * No subclassing needed: new denominations are new instances.
 * Example:
 *   NoteDispenser d100 = new NoteDispenser(100, 10);
 *   NoteDispenser d50  = new NoteDispenser(50, 20);
 *   NoteDispenser d20  = new NoteDispenser(20, 30);
 *   d100.setNextChain(d50);
 *   d50.setNextChain(d20);
 */

@Getter
public class NoteDispenser implements DispenseChain {

    private DispenseChain nextChain;
    private final int denomination;
    private int availableNotes;

    public NoteDispenser(int denomination, int availableNotes) {
        this.denomination = denomination;
        this.availableNotes = availableNotes;
    }

    @Override
    public void setNextChain(DispenseChain nextChain) {
        this.nextChain = nextChain;
    }

    @Override
    public void dispense(int amount) {
        int notesToDispense = Math.min(amount / denomination, availableNotes);
        int remainingAmount = amount - (notesToDispense * denomination);

        if (notesToDispense > 0) {
            System.out.println("Dispensing " + notesToDispense + " x $" + denomination);
            availableNotes -= notesToDispense;
        }

        if (remainingAmount > 0) {
            if (nextChain != null) {
                nextChain.dispense(remainingAmount);
            }
            else {
                System.out.println("Cannot dispense remaining $" + remainingAmount);
                throw new ChangeNotAvailableException(remainingAmount);
            }
        }
    }

    @Override
    public boolean canDispense(int amount) {
        int notesToUse = Math.min(amount / denomination, availableNotes);
        int remainingAmount = amount - (notesToUse * denomination);

        if (remainingAmount == 0) {
            return true;
        }

        return nextChain != null && nextChain.canDispense(remainingAmount);
    }
}
