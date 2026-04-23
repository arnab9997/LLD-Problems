## Functional Requirements
### User flow
* User enters card and enters PIN
  * If Card is ACTIVE, proceed else deny any activity
* User selects action -> Display balance, withdraw cash, deposit cash
* Display balance action:
  * ATM displays balance
* Deposit/Withdraw cash action:
  * Enters amount (transaction is initiated)
  * ATM validates user's balance & cash in ATM
  * If true, dispenses cash, and debits bank balance, marks transaction as complete, generates receipt
  * Else throw exception

### ATM flow
* Interact with bank service to:
  * check user's balance
  * update user's balance

---

## Non-functional requirements
* Extensible - Cash Dispense strategy should be extensible

---

## Core entities
* Card
* Account
* Bank Service
* Cash Dispenser Service (Manages ATM's cash)
* Note Dispenser Chain
* ATM Machine
* TransactionState

---

## Enums
* CardStatus
* OperationType

---

## State Models
* Idle state
* Card Inserted state
* Select Operation state
* Transaction state

---

## Design Patterns
* State Design Pattern - ATM's states
* Chain Of Responsibility Pattern - Note Dispensing chain

---

## API Design
Not required

---

## DB Persistence
* Account(id, balance, daily_limit)
* Transaction(id, account_id, amount, status, timestamps)

---

## NOTES - DIP + ISP - Design Principles

### The Core Mental Model
* Identify who depends on a concrete class (the clients).
* If there are **multiple clients with different needs** → introduce an interface.
* Shape the interface from the **consumer's perspective**, not the implementor's (concrete class).
* If clients have **diverging needs** → split into multiple interfaces.
* The concrete class implements all of them.

### DIP (Dependency Inversion Principle)
**Statement:** High-level modules should not depend on low-level modules. Both should depend on abstractions.

**In practice:**
* Without DIP: `StateClass → ConcreteClass`
* With DIP: `StateClass → Interface ← ConcreteClass`

The dependency arrow from the consumer now points at an abstraction, not an implementation. The concrete class can be swapped, extended, or mocked without touching the consumer.

**Trigger:** You have one or more clients depending directly on a concrete class, and substitutability or isolation matters.

### ISP (Interface Segregation Principle)
**Statement:** Clients should not be forced to depend on methods they do not use.

**In practice:**
* Don't design interfaces from the implementor's perspective ("what can this class do?")
* Design them from the consumer's perspective ("what does this client actually need?")
* If two clients need different subsets → split into two interfaces

**Trigger:** A single interface is growing methods that only some consumers care about.

### DIP + ISP Working Together
1. Spot the concrete class with multiple clients
2. Introduce an interface (DIP - invert the dependency)
3. Keep the interface shaped around what clients actually need (ISP - no dead weight)
4. If client needs diverge significantly, split the interface

### Vending Machine — Why We Introduced `VendingMachineContext`

**Two distinct client groups on `VendingMachine`:**

| Client | Needs |
|---|---|
| State classes | `transitionTo`, `getTransaction`, `executeDispense`, `refundInsertedCoins`... |
| Admin / Customer API | `addProduct`, `refillCoins`, `displayProducts`... |

**Without the interface:** State classes would depend on the full `VendingMachine` - they'd have visibility into admin methods (`addProduct`, `refillCoins`) they should never touch. ISP violated.

**With `VendingMachineContext`:**
* States depend only on the narrow interface - ISP satisfied
* `VendingMachine` implements the interface - DIP satisfied
* Dependency arrow: `State → VendingMachineContext ← VendingMachine`
* States can be unit tested by swapping in a `TestVendingMachine` — no mocking framework needed

Every method on `VendingMachineContext` exists because a state class needs it. Nothing leaked in from the admin side. That's the interface shaped by its consumers.

### ATM — Why We Did NOT Introduce an Interface for `ATMMachine`

* Only **one client group**: state classes
* `ATMMachine` was already lean - no admin methods, no dual-client problem
* No meaningful substitution scenario - a second implementation of `ATMMachine` doesn't exist in production
* ISP not violated - states see only what they need anyway
* DIP not needed - there's nothing to invert when there's one consumer and one implementation

**Introducing an interface here would be speculative abstraction** - indirection without benefit.

## The Smell That Triggers This Pattern

> This concrete class is getting consumed by two different kinds of callers, and one of them is starting to see methods it shouldn't.

That's your signal to introduce an interface, shape it around the consumer that needs protection, and let the concrete class implement it.