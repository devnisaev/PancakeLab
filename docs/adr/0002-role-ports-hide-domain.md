# ADR 0002: Role ports hide the domain

- Status: Accepted
- Date: 2026-09-09

## Context

The assignment says the client API must not expose internal domain objects. Three actors use the shop: Disciple, Chef, Delivery (Sensei extends the menu).

A single fat interface would work for the kata but would let every caller see every command.

## Decision

Publish role ports in `org.pancakelab.api`:

- `DiscipleOrders` — create, toppings, checkout, cancel
- `Kitchen` — completed queue, prepare
- `DeliveryDesk` — prepared queue, deliver
- `PancakeShop` — composition of the above, plus `listOrders` / `addMenuItem`

Return only `UUID`, `List<String>`, `OrderTicket`, and `DeliveryResult`. Never `Order`, `Pancake`, or `Ingredient`.

`ShopKiosk` depends on those ports, not on domain classes.

Presentation filters `listOrders()` by status before prompting:

- view: active tickets (not `DELIVERED`)
- add / remove / checkout: `CREATED` only
- cancel: `CREATED`, `COMPLETED`, or `PREPARED` (until delivery)

Domain rules remain the backstop if a port is called directly.

## Consequences

- Chef cannot “accidentally” add mustard through the kitchen port.
- Disciples are not prompted to edit tickets already sent to the kitchen.
- A future HTTP adapter can implement the same ports.
- `PancakeService` still implements all roles — ISP is for callers, not a microservice split.
