# Architecture Decision Records

Format: context → decision → consequences. Status is **Accepted** unless noted.

| ADR | Decision |
|---|---|
| [0001](0001-in-memory-order-store.md) | `ConcurrentHashMap` as the order store |
| [0002](0002-role-ports-hide-domain.md) | Role ports; API never returns `Order` |
| [0003](0003-shop-exception-taxonomy.md) | `ShopException` for shop rules |
| [0004](0004-domain-events-for-side-effects.md) | Domain events instead of logging inside `Order` |
| [0005](0005-catalog-not-pancake-builders.md) | Incremental toppings; no pancake builders |
| [0006](0006-file-order-archive.md) | File logs and delivery statements for finished orders |
