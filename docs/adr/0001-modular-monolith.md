# 1. Start as a modular monolith

- Status: Accepted
- Date: 2026-11-17

## Context

The domain has several clear areas (businesses, staff, booking, waitlist, notifications).
They could be built as separate microservices, but the project is built by one developer,
the expected load is small, and the most important rule (a slot is never given to two
people) spans booking and waitlist and benefits from a single database transaction.

## Decision

Build one Spring Boot application, split into modules by package. Use Spring Modulith to:

- verify in a test that modules only use each other's public API and have no cycles,
- publish domain events between modules and store them in an event publication table,
- generate module documentation.

Use one PostgreSQL database. Each table belongs to exactly one module, and other modules
do not query it directly.

## Alternatives considered

- **Microservices from day one.** Brings distributed transactions, network failures,
  service discovery and multiple deployments before the core logic exists. Rejected.
- **A plain layered monolith** (`controller` / `service` / `repository` packages).
  Simple, but nothing prevents the waitlist code from reaching into booking internals,
  and the code becomes hard to split later. Rejected.

## Consequences

- One build, one deployment, local transactions across modules when needed.
- Module boundaries are enforced by a test, not only by convention.
- A module can be extracted into its own service later because it already talks to others
  through an API and events.
