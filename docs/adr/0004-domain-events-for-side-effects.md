# ADR 0004: Domain events for side effects

- Status: Accepted
- Date: 2026-09-09

## Context

An unbounded `StringBuilder` “order log” mixed formatting with use cases, grew forever, and ran under the order lock. Production-like logging without Log4j was required.

Logging, kitchen boards, and metrics are not pancake invariants.

## Decision

- `Order` records `OrderEvent` facts and exposes `drainEvents()`.
- `PancakeService` publishes drained events **after** the repository mutation (failed commands do not journal).
- Listeners (`ShopJournal`, `FileOrderArchive`, `KitchenBoard`, `ShopMetrics`) implement `OrderEventListener`.
- Lines are logfmt via JDK `System.Logger`. A bounded journal is for tests/diagnostics, not an infinite buffer.

Do not event-source the aggregate or put JUL inside `Order`.

## Consequences

- Side effects can be added without changing lifecycle rules.
- Formatting/I/O no longer blocks other toppings on the same lock for longer than draining a small list.
- Listener failures must not fail the order (`SystemShopJournal` swallows logger errors).
