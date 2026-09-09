# Java Architect skills in Pancake Lab

UML in this folder shows **structure**. This note captures **architect judgment**: what we chose, why, and what we refused. That is the skill set a Java architect is expected to explain on this codebase.

Diagrams: [architecture-overview](architecture-overview.puml), [layers](architecture-layers.puml), [ports and adapters](architecture-ports-adapters.puml), [patterns](architecture-patterns.puml), [events](architecture-events.puml), [kiosk](architecture-kiosk.puml), [domain](domain.puml), [order lifecycle](order-lifecycle.puml).

Decisions: [Architecture Decision Records](adr/README.md). Quality: [quality attributes and threats](quality.md).

## Skills already demonstrated

| Skill | What it looks like here | Why it matters |
|---|---|---|
| **Bounded context** | One shop: order → kitchen → delivery. No “user service”, no HTTP. | An architect draws the *smallest* system that still matches the business. |
| **Ubiquitous language** | `Order`, `Pancake`, `Ingredient`, `Address`, `ShopException` — not `OrderManager` / `EntityHelper`. | Names in code match Sensei, Disciple, Chef. |
| **Rich aggregate** | `Order` owns pancakes, status, version, and events. No `order.setStatus(PAID)`. | Business rules live in the model, not in a CRUD service. |
| **Value objects** | `Address`, `Ingredient` validate themselves; equality by value. | Invalid rooms and mustard never become domain state. |
| **Domain services vs application services** | `IngredientCatalog` / `AddressRegistry` are catalogs. `PancakeService` only orchestrates. | Catalogs are not “god services”. |
| **Repository as a port** | `OrderRepository` in domain; `InMemoryOrderRepository` is the adapter. | Persistence can change without rewriting use cases. |
| **Anti-corruption / API boundary** | Ports return `UUID`, `List<String>`, `OrderTicket`, `DeliveryResult`. Never `Order`. | Callers cannot couple to internals. |
| **Interface segregation by actor** | `DiscipleOrders`, `Kitchen`, `DeliveryDesk`. | Chef does not see “add topping”. |
| **Open/closed for recipes** | New topping = catalog entry, not `HazelnutPancake`. | Fu Man Chu’s mustard problem was hardcoded types. |
| **State machine** | `OrderStatus.canTransitionTo` / `allowsEditing`. | Lifecycle is one place, not `if`s in the service. |
| **Exception taxonomy** | `ShopException` for shop rules; `IAE`/`NPE` for wiring and typed junk. | Kiosk can show `!` for business errors and still crash on bugs. |
| **Concurrency at the aggregate** | Per-order lock / `compute`; optimistic version on status reads. | Isolation unit is the order, not the whole shop. |
| **Storage choice** | `ConcurrentHashMap<UUID, Order>` as system of record. Scan-by-status is enough. | Map for identity lookup; queues only if kitchen listing is hot. |
| **Domain events + listeners** | `Order` records events; journal / kitchen board / metrics subscribe. | Side effects stay outside the aggregate and off the lock. |
| **Hexagonal, partially** | Driving: kiosk. Driven: in-memory repo + journals. | Ports in `api`/`domain`; adapters in `service`/`logging`. |
| **Layered dependency rule** | Domain does not import console, DTOs, or JUL. | Outer rings depend inward. |
| **YAGNI / simplicity** | No Spring, no DB, no CQRS, no event store. | Architect skill is *stopping*. |
| **TDD as design** | Tests first; production stays the minimum that stays green. | Design is forced by behaviour, not by diagrams. |
| **Presentation vs domain** | `KioskBoard` formats receipts; domain still says `Delicious pancake with …!`. | UI can be handsome without polluting the model. |

## Skills worth adding next (docs, not more code)

ADRs: [adr/](adr/README.md). Quality attributes and threats: [quality.md](quality.md).

What is still useful as a short note later:

### 1. Evolution / strangler notes

What *would* change if this left the kata:

- `OrderRepository` → JDBC adapter (same port)
- `ShopKiosk` → HTTP adapter on the same role ports
- Kitchen/delivery listing → secondary queues if the scan is slow
- `ShopJournal` → JSON lines / OpenTelemetry without touching `Order`

That is the payoff of ports: **replace adapters, keep the aggregate**.

## Cursor skills (optional, separate from `docs/`)

`docs/` is for humans and reviews. Agent skills live in `.cursor/skills/` and fire when coding.

Useful project skills, if we add them later:

| Skill | When it should run |
|---|---|
| `pancake-lab-architect` | Changing packages, ports, or persistence |
| `ddd-review` | PR-style review: aggregate boundaries, no domain leak |
| `concurrency-review` | Touches `OrderRepository` or shared maps |
| `adr-writer` | User asks “why did we…” or makes a structural choice |

Rules already cover day-to-day DDD/TDD (`.cursor/rules/`). Skills should encode **review checklists**, not repeat “use Java 17”.

## How to talk about this in an interview

A short architect narrative for this repo:

1. One bounded context, rich `Order` aggregate.
2. Ports per actor so the API cannot leak domain objects.
3. Catalogs as the only way names and rooms enter the model.
4. Concurrency at order id; map as the store; events for logging and boards.
5. `ShopException` vs programming errors.
6. Stop before frameworks.

If you cannot point at a class for each sentence, the architecture is decoration. Here you can.
