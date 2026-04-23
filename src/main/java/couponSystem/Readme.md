## Functional Requirements
* A shopping cart holds an "ordered" list of products, each with a name, price, and type.
* Multiple coupons can be registered with the cartCouponService and are applied **in registration order**.
* Each coupon reduces item prices in place; later coupons see prices already reduced by earlier ones (stacking).
* The cartCouponService computes the final cart total after all coupons have been applied.
* Three coupon types are supported:
    - **PercentageOffAll** — applies a flat percentage discount to every item in the cart.
    - **NthItemTypeDiscount** — discounts every Nth occurrence of a given product type (e.g. every 2nd FURNITURE item gets 20% off).
    - **NextItemPercentageOff** — when a trigger-type item is encountered, the immediately following item gets a percentage discount; the discounted item is then skipped as a potential trigger.

---

## Non-Functional Requirements
* Coupon application must be **deterministic** — same cart + same coupon registration order always produces the same total.
* The system must be **open for extension** — new coupon types must be addable without modifying existing classes.
* Catalog data (`Product`) must be **immutable**; only the cart-scoped `LineItem` holds mutable pricing state.

---

## Core Entities
* Product
* LineItem
* ShoppingCart
* CartPriceService
* Coupon

---

## Enums
* ProductType

---

## State Models
N/A

---

## Design Patterns
* Strategy Design Pattern - `Coupon` is a Strategy interface. Each coupon type (`PercentageOffAllCoupon`, `NthItemTypeDiscountCoupon`, `NextItemPercentageOffCoupon`) is an independent Strategy implementation. `CartPricingService` composes them without knowing their internals.
* Chain of Responsibility - considered, rejected
  * CoR implies a handler can short-circuit and stop the chain. Here, every coupon always runs - the pipeline never aborts early. A simple loop over strategies is the correct model. CoR adds handler-chaining machinery with no benefit here.
* Decorator - considered, rejected
  * Decorator works when you're adding behavior to a single object's interface by wrapping it - `CouponDecorator(CouponDecorator(Product))`. This is a fundamental misfit here for two reasons:
    * Coupons aren't enriching a product's interface - they're **transforming cart-level state**.
    * Coupon types (`NextItemPercentageOff`, `NthItemTypeDiscount`) are **cart-level concerns** - they need to see multiple products simultaneously. A decorator wrapping a single `Product` can never see the rest of the cart. You hit this wall immediately with these two coupon types.


---

## Concurrency
N/A

---

## API Design
* CartPricingService
  * addCoupon(Coupon coupon) - registers a coupon; order of registration = application order
  * calculateTotal(ShoppingCart cart) - returns total after all coupons applied

---

## DB Persistence
N/A

---

## Notes

- **Coupon ordering is the caller's responsibility.** `PercentageOffAll` applied before a type-specific coupon compounds differently than after. This is by design — the engine makes no attempt to reorder or resolve conflicts.
- **`NextItemPercentageOffCoupon` is position-sensitive.** It relies on stable insertion order in the cart. If cart ordering semantics ever change (e.g. sorted by price), this coupon's behaviour changes silently. This assumption is documented on the class.






Decorator — worth mentioning, but reject it
Decorator would wrap the cart repeatedly — CouponDecorator(CouponDecorator(Cart)). This gets messy because coupons aren't enriching an object's interface, they're transforming state. CoR + Strategy is cleaner. Mention this tradeoff if the interviewer probes.

What's wrong with the Decorator approach hereFundamental misfit: Decorator works when you're adding behavior to a single object's interface. Here, coupons like "P% off next item" or "Nth item of type T" are cart-level concerns — they need 
to see multiple products simultaneously. A decorator wrapping a single Product can never see the rest of the cart. You hit this wall immediately with coupon types 2 and 3.

One thing to nail in the interview
The interviewer will likely ask: "What does each coupon receive — the whole cart, or individual items?"
Your answer: each coupon receives a List<LineItem> (a flat view of the cart with quantities expanded). This makes coupon 2 ("P% off next item") and coupon 3 ("Nth item of type T") tractable — they can iterate through line items with an index. If you pass a Map<Product, quantity>, implementing these becomes awkward.



I’d use Strategy Pattern to encapsulate different coupon behaviors, Chain of Responsibility to apply them sequentially, and a Factory Pattern to create coupon objects dynamically. If needed, I can extend with Decorator for composable pricing and Composite for complex cart structures. I’ll also explicitly maintain ordering of coupons and avoid mutating state blindly to ensure correctness and extensibility.


