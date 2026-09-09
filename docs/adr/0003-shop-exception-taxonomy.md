# ADR 0003: Shop exception taxonomy

- Status: Accepted
- Date: 2026-09-09

## Context

Shop failures (unknown topping, missing building, illegal lifecycle) were named types that still extended `IllegalArgumentException`. The kiosk caught `RuntimeException`, so a null-pointer looked like a menu message.

True programming errors (empty catalog in a constructor, `null` order id, typed `abc` for a number) are not shop rules.

## Decision

- Shop rules extend unchecked `ShopException` (`UnknownIngredientException`, `InvalidLocationException`, `OrderNotFoundException`, `PancakeNotFoundException`, `InvalidRemovalCountException`, `DuplicateIngredientException`, `IllegalOrderStateException`).
- Constructors, parse errors in the kiosk, and `null` arguments stay `IllegalArgumentException` / `NullPointerException`.
- The kiosk prints `!` for `ShopException` and parse `IAE` only.

No checked exceptions, error codes, or a 15-type hierarchy.

## Consequences

- Callers can `catch (ShopException)` for recoverable “tell the disciple” cases.
- Wiring bugs still abort instead of looking like Fu Man Chu.
- Tests assert the named type, not `IllegalArgumentException`, for pancake-not-found and non-positive remove count.
