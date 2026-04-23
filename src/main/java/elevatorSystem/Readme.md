## Functional Requirements
* Support multiple elevators operating concurrently
* Handle external requests (floor + direction)
* Handle internal cabin requests (target floor selection)
* Assign the most suitable elevator for external requests using a dispatch strategy
* Move elevators based on SCAN scheduling algorithm
* Stop at requested floors and serve requests in-order
* Provide real-time updates via observer (console display)
* Support graceful startup and shutdown of the system
---

## Non-functional requirements
* Thread-safe handling of shared elevator state
---

## Core entities
* ElevatorCar
* ElevatorSystem
* Request
* ElevatorDispatchStrategy
* ElevatorObserver

---

## Enums
* Direction
* RequestSource

---

## State Models
* IdleState
* MovingUpState
* MovingDownState

---

## Design Patterns
* Strategy Design Pattern - For elevator dispatch logic 
* State Design Pattern - To encapsulate movement behaviour based on elevator state
* Observer Design Pattern - Decouple elevator events from consumers
* Facade Design Pattern - ElevatorSystem provides a simplified interface to the system

---

## Concurrency
* Each ElevatorCar runs in its own thread
* Shared mutable states:
  * currentFloor
  * state
  * stops[]
* Synchronization strategy:
  * All mutations guarded via synchronized(this)
  * Ensures atomicity of:
    * request addition
    * movement
    * state transitions
* Dispatch strategy performs non-atomic reads:
  * May result in stale decisions
  * Tradeoff: acceptable for interview scope (affects optimality, not correctness)
* Observer notifications:
  * Executed outside synchronized block
  * Prevents deadlocks due to callback re-entry
* No cross-elevator locking:
  * Eliminates risk of deadlocks

---

## API Design
* start()
* requestElevator(int floor, Direction direction)
* selectFloor(int elevatorId, int floor)
* shutdown()

---

## DB Persistence
*

---

## Notes
* Uses SCAN (elevator algorithm) via boolean[] stops
  * Efficient and naturally deduplicates requests
* Requests are not queued globally
  * If no suitable elevator is found → request is dropped
  * Tradeoff for simplicity (should be queued in production)
* RequestSource enum exists but is not fully utilized
  * Internal vs external requests are inferred indirectly
* Scheduling logic is tightly coupled to ElevatorCar
  * Limits extensibility for alternative scheduling algorithms
* Concurrency model is coarse-grained but safe
  * Can be optimized with finer locks or lock-free reads
* Designed intentionally within LLD interview constraints
  * Prioritizes clarity and correctness over full production completeness