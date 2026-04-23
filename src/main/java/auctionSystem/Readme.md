## Functional Requirements
* Create users who can participate in auctions
* Create auction listings with item name and starting price
* Place bids on an active auction
* Validate bids:
  * Bid must be greater than current highest bid (or starting price if no bids exist)
* Notify previous highest bidder when outbid
* Notify all participants when auction ends

---

## Non-functional requirements
* Thread-safe bid placement for concurrent users
* Consistent auction state (no invalid transitions like bidding on closed auctions)

---

## Core entities
* User
* AuctionListing
* Bid
* AuctionHouseMediator
* AuctionObserver

---

## Enums
* AuctionStatus

---

## State Models
* N/A

---

## Design Patterns
* Mediator Design Pattern
  * AuctionHouseMediator centralizes interaction between users and auctions
  * Prevents direct coupling between ```User``` and ```AuctionListing```
* Observer Design Pattern
  * Users subscribe to auction updates
  * Notifications on outbid events, auction closure
* Singleton Design Pattern: * ```AuctionService``` to ensure single coordination point

---

## Concurrency
* Shared mutable states:
  * Auction listings
  * Bid list within each auction
* Thread-safety mechanisms:
  * ConcurrentHashMap for user and auction storage
  * synchronized methods (placeBid, endAuction) at auction level
* Locking strategy:
  * Fine-grained locking per AuctionListing
  * Prevents race conditions during bid placement

---

## API Design
* placeBid(auctionId, amount)

---

## DB Persistence
*

---

## Notes