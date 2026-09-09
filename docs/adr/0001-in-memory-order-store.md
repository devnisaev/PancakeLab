# ADR 0001: In-memory order store

- Status: Accepted
- Date: 2026-09-09

## Context

The kata requires an in-memory “database”. Orders are looked up by id (add topping, checkout, prepare, deliver) and disappear when cancelled or delivered. Kitchen/delivery listing is by status. Concurrent disciples must not corrupt the same order.

## Decision

Keep `ConcurrentHashMap<UUID, Order>` as the system of record behind `OrderRepository`.

- Identity access is O(1).
- Per-order locking (`compute` / lock) isolates one aggregate.
- `take()` removes the row atomically with the last mutation.

Do not use a queue, `CopyOnWriteArrayList`, or a single lock around the whole shop as the primary store. Do not add JDBC, files, or Redis.

List-by-status may scan live orders. Secondary queues (kitchen / delivery) only if that scan becomes a hotspot.

## Consequences

- Process restart loses all tickets — acceptable for the dojo.
- Swapping to JDBC later is an adapter change; `PancakeService` and `Order` stay.
- Indexes must stay consistent with the map if we add them; until then, a scan is cheaper than two structures.
