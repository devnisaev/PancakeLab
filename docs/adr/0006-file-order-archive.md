# ADR 0006: File archive for terminal orders

- Status: Accepted
- Date: 2026-09-09

## Context

Cancelled and delivered tickets disappear from the in-memory store. The Sensei still needs a paper trail: which rooms were cancelled, what was delivered, and a delivery statement listing pancakes.

JDK `System.Logger` is ephemeral. Persistence of orders is still out of scope.

## Decision

Add `FileOrderArchive` as an `OrderEventListener` (not inside `Order`).

- `shop-log/events.log` — Created, Completed, Prepared, Cancelled, Delivered
- `shop-log/cancelled.log` — cancelled tickets
- `shop-log/delivered.log` — delivered tickets
- `shop-log/statements/{orderId}.txt` — one delivery statement with pancake descriptions

`Delivered` carries the pancake list so the statement does not read the aggregate after `take()`. Topping noise stays off disk. Archive I/O failures do not fail the order. `PancakeService.logged()` composes `SystemShopJournal` with this archive.

## Consequences

- Process restart still loses live tickets; history of finished ones can remain on disk.
- `shop-log/` is working-directory state, gitignored.
- A JDBC adapter would replace this listener, not `Order`.
