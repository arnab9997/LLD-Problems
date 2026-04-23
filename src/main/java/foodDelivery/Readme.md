## Functional Requirements
* Register customers, restaurants, and delivery agents
* Browse restaurant menus
* Search restaurants using multiple filters:
  * City
  * Proximity
  * Menu keyword
* Place orders with multiple items
* Track order lifecycle: PENDING → CONFIRMED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED
* Cancel order (only in valid states)
* Assign delivery agent automatically
* Notify stakeholders (customer, restaurant, agent) on order updates

---

## Non-functional requirements
* Thread Safety
  * Order state transitions are synchronized
  * Delivery agent assignment avoids race conditions using CAS
* Scalability
  * ConcurrentHashMap used for orders
  * Stateless strategies allow easy horizontal scaling later
* Extensibility
  * Strategy pattern for
    * Delivery assignment
    * Restaurant search

---

## Core entities
* Address
* Customer
* DeliveryAgent
* MenuItem
* Menu
* Restaurant

---

## Enums
* OrderStatus

---

## State Models
N/A

---

## Design Patterns
* Strategy Design Pattern - For Delivery Assignment & Restaurant Search 
* Observer Design Pattern - For notifying of any order state change
* Singleton Design Pattern - Double-checked locking for FoodDeliveryService

---

## Concurrency
Shared state: Agent Assignment Race Condition
* Classic bug:
  * Thread A and B both see agent available
  * Both assign same agent
* Solution: CAS (Compare-And-Set): ```available.compareAndSet(true, false)```
  * Only one thread succeeds
  * Others automatically fallback to next agent
* Assignment Pipeline
  * Filter available agents (non-binding hint)
  * Sort by distance
  * Atomically claim agent using CAS
This removes TOCTOU bugs completely.

---

## API Design
* Registration
  * registerCustomer(name, phone, address)
  * registerRestaurant(name, address)
  * registerDeliveryAgent(name, phone, location)
* Order Lifecycle
  * placeOrder(customerId, restaurantId, items)
  * startPreparingOrder(orderId)
  * markOrderReadyForPickup(orderId)
  * markOrderDelivered(orderId)
  * cancelOrder(orderId)
* Search & Discovery
  * searchRestaurants(List<RestaurantSearchStrategy>)
  * getRestaurantMenu(restaurantId)
---

## DB Persistence
* Orders
* Restaurants
* Agents
---

## Notes
* 