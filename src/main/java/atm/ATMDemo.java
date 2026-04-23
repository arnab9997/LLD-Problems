package atm;

import atm.enums.OperationType;
import atm.services.BankService;
import atm.services.CashDispenserService;

public class ATMDemo {

    public static void main(String[] args) {

        // ============================ Bootstrap ============================
        BankService bankService = new BankService();
        bankService.createAccount("ACC001", 5000.0);
        bankService.createCardAndLinkToAccount("CARD001", "1234", "ACC001");

        // FIX: Create a second account+card upfront for scenarios after CARD001 gets blocked.
        bankService.createAccount("ACC002", 1000.0);
        bankService.createCardAndLinkToAccount("CARD002", "5678", "ACC002");

        CashDispenserService dispenserService = new CashDispenserService(10, 20, 30);
        ATMMachine atm = new ATMMachine(bankService, dispenserService);

        // ============================ Happy Path ============================
        System.out.println("\n=== Happy Path ===");
        atm.insertCard("CARD001");
        atm.enterPin("1234");
        atm.selectOperation(OperationType.CHECK_BALANCE);
        atm.selectOperation(OperationType.WITHDRAW_CASH);
        atm.performTransaction(200);
        atm.selectOperation(OperationType.CHECK_BALANCE);
        atm.ejectCard();

        // ============================ Wrong PIN → card blocked ============================
        // After this section CARD001 is permanently blocked.
        System.out.println("\n=== Wrong PIN (3 attempts → card blocked) ===");
        atm.insertCard("CARD001");
        atm.enterPin("9999");
        atm.enterPin("8888");
        atm.enterPin("7777"); // 3rd failure → card blocked, session reset

        // ============================ Verify blocked card is rejected ============================
        System.out.println("\n=== Blocked card re-insertion (should be rejected) ===");
        atm.insertCard("CARD001"); // stays in IdleState — no session started

        // ============================ Invalid card ============================
        System.out.println("\n=== Invalid card ===");
        atm.insertCard("CARD_UNKNOWN");

        // ============================ Invalid state transition ============================
        System.out.println("\n=== Invalid state transition ===");
        try {
            atm.enterPin("1234"); // ATM is Idle, card not inserted
        } catch (Exception e) {
            System.out.println("Caught expected error: " + e.getMessage());
        }

        // ============================ Deposit ============================
        // FIX: Use CARD002 — CARD001 was blocked above and would be rejected by IdleState.
        System.out.println("\n=== Deposit ===");
        atm.insertCard("CARD002");
        atm.enterPin("5678");
        atm.selectOperation(OperationType.DEPOSIT_CASH);
        atm.performTransaction(500);
        atm.selectOperation(OperationType.CHECK_BALANCE);
        atm.ejectCard();
    }
}