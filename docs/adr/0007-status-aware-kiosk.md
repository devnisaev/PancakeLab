# ADR 0007: Status-aware order desk in ShopKiosk

- Status: Accepted
- Date: 2026-09-10

## Context

The domain already blocks pancake edits after checkout (`OrderStatus.allowsEditing()` / `requireOpen()`). Cancel is allowed until delivery (`CREATED`, `COMPLETED`, `PREPARED` → `CANCELLED`).

The console still listed every in-memory ticket for every disciple action, so completed orders looked editable even when the API would reject add/remove or succeed on cancel.

## Decision

Filter `PancakeShop.listOrders()` in `ShopKiosk` before prompting:

| Action | Tickets offered |
|---|---|
| View orders | Active only (exclude `DELIVERED`) |
| Add pancake / remove / checkout | `CREATED` only |
| Cancel | `CREATED`, `COMPLETED`, `PREPARED` |

When a filtered list is empty, show a notice (`No open orders to edit`, `No orders can be cancelled`, `No active orders`) instead of a pick prompt.

Kitchen and delivery screens keep filtering by `COMPLETED` and `PREPARED`.

## Consequences

- Presentation guides disciples; domain rules remain the backstop for direct API callers and tests.
- Delivered and cancelled tickets disappear from the store (`take()`); the active view is defensive for any future `DELIVERED` rows.
- Delivery statements and file logs are unchanged — still written on `Delivered`, not on checkout.
