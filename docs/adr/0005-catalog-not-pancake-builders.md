# ADR 0005: Catalog toppings, no pancake builders

- Status: Accepted
- Date: 2026-09-09

## Context

Disciples invented toppings the code did not model. Hardcoded recipe classes and a mustard pancake from Fu Man Chu followed. The Sensei forbade pancake builders passed into the API; toppings must be added one by one. Recipes must not be hardcoded.

## Decision

- A pancake is a list of `Ingredient` values on `Order`, started with `addPancake()` then `addIngredient`.
- The menu is `IngredientCatalog` (allowlist). Unknown names throw `UnknownIngredientException`.
- Sensei may `addMenuItem`; duplicates throw `DuplicateIngredientException`.
- Buildings/rooms go through `AddressRegistry`, not raw ints in the aggregate.

No `DarkChocolateHazelnutPancake` type, no builder on the API, no mustard unless it is on the menu.

## Consequences

- New toppings do not require new classes or a rebuild of recipe hierarchies.
- The catalog is the control against invented ingredients; the registry is the control against ghost buildings.
- UI may pretty-print “dark chocolate, hazelnuts”; the domain description stays the full sentence.
