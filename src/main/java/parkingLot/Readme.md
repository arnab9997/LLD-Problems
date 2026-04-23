## Functional Requirements
* Support multiple parking floors, each managing it's own parking spots.
* Support multiple vehicle types: TWO_WHEELER, FOUR_WHEELER, EIGHT_WHEELER.
* Each vehicle type maps directly to a required ParkingSpotType.
* The system should handle multiple entry and exit points and support concurrent access.
* Automatically assign parking spot based on availability upon entry and release it when the vehicle exits.
* Upon entry:
  * Issue a parking ticket with vehicle details
  * Generate a ticket with Unique ID and entry time
* On exit:
  * Validate ticket and vehicle
  * Calculate fees with "configured strategy"
  * Process payment
  * Free the parking spot 
* Support querying and displaying real-time availability of parking spots, grouped by floor and spot size.

## Non-functional Requirements
* The design should follow object-oriented principles with clear separation of concerns.
* The system should handle concurrent entry/exit events without race conditions.
* The system should be modular and extensible to support future enhancements.
* The code should be thread-safe for concurrent access.
* The components should be testable in isolation.

## Core Entities
* Parking Lot
* Parking Level
* Parking Spot
* Vehicle
* Ticket
* Payment
* Entry Gate
* Exit Gate
* Parking Spot Service
* Ticket Service

## Enums
* VehicleType
* ParkingSpotType
* ParkingSpotStatus
* TicketStatus
* PaymentStatus

## State models
* TicketStatus: ACTIVE -> PAID -> COMPLETED
* ParkingSpotStatus: UNOCCUPIED -> OCCUPIED

## Design Patterns
* Strategy Design Pattern - For payment, parking, fee calculation strategy
* Factory Design Pattern - Encapsulates vehicle creation logic (done via VehicleType.createVehicle) 

## Concurrency
### Shared mutable state
* Available spots
* Ticket store

### Invariant
* No double allocation
### Mechanism

### Notes
  Cannot lock in ParkingLot.parkVehicle() using: public synchronized Ticket parkVehicle(...)
  Because then even though different parkingLevels can allocate in parallel, this system would turn into a single-lane toll booth.
  But, locking at parkingLevel granularity balances correctness and throughput, so we avoid global lock at parking lot.
  NOTE: Locking can also be done at ParkingSpot parkingLevel, but since we're looping the list, this might be overkill
  
  If the DB is doing concurrency:
  🚫 no synchronized 
  🚫 no ReentrantLock
  🚫 no in-memory occupied flag as source of truth

## API Design
* Ticket parkVehicle(Vehicle vehicle)
* Ticket unParkVehicle(UUID ticketID, Vehicle vehicle)
* AvailabilityInfo getAvailability()

=====================================
* Idempotency for payment
* Exception handling

## Db Persistence
* parking_spot
  * id
  * type
  * status
  * level_id
  * version (for locking)
* ticket
  * id
  * spot_id
  * vehicle_number
  * status
  * entry_time
  * exit_time
* Indexes:
  - parking_spot: (spot_id, status)
  - ticket: (vehicle_number, status)