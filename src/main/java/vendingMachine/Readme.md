## Functional Requirements
### User flow
* Vending machine should display products with:
  * Name
  * Product Code
  * Price
  * Quantity available
* User selects a product by entering the product code
* User inserts coins
  * Machine should accept coins of different denominations.
* User can cancel the transaction before dispensing -> machine issues full refund.
* Vending machine dispenses product when:
  * Sufficient money inserted
  * Item in stock
* The machine dispenses the selected product and returns change if necessary.
- Vending machine returns to idle state

### Machine Admin flow
- Add new products
- Restock products
- Refill coins

## Non-functional requirements
* Maintainability:
  * Follows SOLID behaviour
  * Clear separation of state Behaviour
  * Ease of extension
* Atomicity: The purchase operation must be atomic:
  * Either:
    * Product is dispensed
    * Stock updated
    * Coin inventory updated
    * Correct change returned
  * Or:
    * Full refund
    * No inventory mutation

## Core entities
* Product
* Transaction
* VendingMachine
* ProductInventory
* CoinInventory
* VendingMachineState
* ChangeCalculatorStrategy

## Enums
* Coin
  * ONE, TWO, FIVE, TEN
* ProductTransactionState
  * INITIATED, PAYMENT_IN_PROGRESS, DISPENSING, COMPLETED, CANCELLED, FAILED

## State Models
* Idle State
* Product Selection State
* Amount Accept State
* Dispensing State

## Design Patterns
* State Design Pattern - Each state encapsulates user flow, operation validity, transition rules.
* Strategy Design Pattern - For payment strategy

## API Design
* User APIs:
  * List<Product> displayProducts()
  * void selectProduct(String productCode)
  * void insertCoin(Coin coin)
  * void cancelTransaction()
  * void dispense()
* Vending Machine APIs:
  * void addProduct(Product product, int quantity)
  * void restockProduct(Product product, int quantity)
  * void refillCoins(Map<Coin, Integer> coins)

## DB Persistence
* Not required for this system

## NOTES
This implementation contains the State Design pattern coupled with the Dependency Inversion Principle (DIP):

### The Base Pattern: State (GoF)
The core structure is:
* VendingMachine = Context - holds the current state, delegates events to it.
* VendingMachineState = State interface - defines the events the machine can receive.
* IdleState, DispensingState, etc. = Concrete States - implement behavior per state, trigger transitions back on the context.

### The Additional Layer: Dependency Inversion Principle (DIP)
Current implementation where states depend on VendingMachineContext (an interface) rather than the concrete VendingMachine - is Dependency Inversion applied to the State pattern.
Without it: ```ConcreteState --depends on--> VendingMachine (concrete)```
With it: ```ConcreteState --depends on--> VendingMachineContext (abstraction) <-- VendingMachine implements```

The VendingMachineContext interface is also an application of Interface Segregation Principle (ISP) - states get a narrow interface with only what they
need (transitionTo, executeDispense, refundInsertedCoins), not the full VendingMachine surface which includes admin methods like addProduct and refillCoins.