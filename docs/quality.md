# Quality attributes and threats

What this kata **optimizes**, what it **ignores**, and which README incidents the model already blocks. Decisions live in [adr/](adr/README.md).

## What we optimize

| Attribute | Stance | Where it lives |
|---|---|---|
| Correctness of lifecycle | Hard invariant | `OrderStatus.canTransitionTo`, `Order.complete` / `prepare` / `cancel` |
| Valid toppings and rooms | Hard invariant | `IngredientCatalog.require`, `AddressRegistry.require` |
| API does not leak domain | Required | Role ports return `UUID`, `List<String>`, `OrderTicket`, `DeliveryResult` |
| Thread safety per order | Required | Per-order lock in `InMemoryOrderRepository`; `volatile` status/version on `Order` |
| Failed commands stay silent | Required | Events drain only after a successful mutation |
| Clarity of shop failures | Required | `ShopException` for rules; `IAE` / `NPE` for wiring |

## What we ignore

| Attribute | Stance |
|---|---|
| Durability after process exit | Out of scope — in-memory map is the store |
| Throughput / latency SLOs | Irrelevant at dojo scale; a status scan is enough |
| Horizontal scale / multi-process | Out of scope |
| Audit replay / event sourcing | Listeners are side effects, not a log to rebuild `Order` |
| Pretty receipts as domain state | `KioskBoard` formats; `Order` still describes pancakes |

Architects are scored on this trade-off, not on adding Redis.

## Threats we designed against

| Incident (README) | Control |
|---|---|
| Mustard on a chocolate pancake | Allowlist catalog; unknown topping → `UnknownIngredientException` |
| Invented recipes / hardcoded types | Incremental `addIngredient`; no pancake builders on the API |
| Delivery to a missing building | `AddressRegistry.require` → `InvalidLocationException` |
| Missing pancakes / lost updates | Aggregate lock on writes; stable pancake ids |
| Cancelled or delivered still in the “DB” | `take()` mutates then removes the row |
| Illegal kitchen or delivery steps | `OrderStatus` refuses the transition → `IllegalOrderStateException` |
| Shop error looking like a crash (or the reverse) | Kiosk prints `!` for `ShopException`; `NPE` still aborts |

## What we will not do

- No Spring, JPA, REST, or Lombok
- No anemic `Order` + setter service
- No microservices for kitchen vs delivery
- No generic `Repository<T, ID>`
- No kitchen/delivery queues until a status scan is actually hot
