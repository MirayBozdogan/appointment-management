# appointment-management

[![CI](https://github.com/MirayBozdogan/appointment-management/actions/workflows/ci.yml/badge.svg)](https://github.com/MirayBozdogan/appointment-management/actions/workflows/ci.yml)

Appointment scheduling for small service businesses (barbers, clinics, studios), with a
**waitlist that automatically fills cancelled slots**.

When an appointment is cancelled, the freed slot is offered to the first matching customer on
the waitlist for a limited time. If they decline or the offer expires, it moves on to the next
customer. The slot is never given to two people, and pending offers survive application
restarts.

> **Status:** early development. The design is documented below and implementation is in progress.

## Why this project is interesting

Most booking apps are CRUD. This one focuses on the parts that are easy to get wrong:

- **No double booking under concurrency.** Overlapping appointments for the same staff member
  are rejected by a PostgreSQL exclusion constraint, not only by application code.
  See [ADR 0002](docs/adr/0002-db-level-double-booking-prevention.md).
- **Time-limited offers that survive restarts.** Offer deadlines are stored in the database and
  processed by a lock-safe scheduler. In-memory timers are never the source of truth.
  See [ADR 0003](docs/adr/0003-durable-offer-expiry.md).
- **Atomic offer acceptance.** Accepting an offer and expiring it can never both succeed.
- **Multi-tenant isolation.** Users of one business cannot read another business's data.
- **Time zones done right.** Working hours are stored in the business's local time and
  appointments are stored as UTC instants. See [ADR 0004](docs/adr/0004-time-zone-handling.md).

## Tech stack

| Area      | Choice                                                   |
|-----------|----------------------------------------------------------|
| Backend   | Java 21, Spring Boot 4, Spring Modulith, Spring Security |
| Database  | PostgreSQL 18, Flyway                                    |
| Frontend  | Next.js, TypeScript                                      |
| Testing   | JUnit 5, Spring Boot Test, Testcontainers                |
| CI        | GitHub Actions                                           |
| Later     | RabbitMQ (transactional outbox), Docker images           |

## Documentation

- [Architecture and module boundaries](docs/architecture.md)
- [Data model and state machines](docs/data-model.md)
- [Architecture Decision Records](docs/adr/)

## Roadmap

- [x] Requirements, data model and module boundaries
- [x] Spring Boot + PostgreSQL + Flyway foundation, CI
- [x] Businesses, services, staff and working hours
- [x] Availability and booking with database-level conflict prevention
- [ ] Cancellation, waitlist and time-limited offers
- [ ] Authentication, roles and per-business access control
- [ ] Next.js customer booking flow and business dashboard
- [ ] Notifications via RabbitMQ with a transactional outbox
- [ ] Docker images and deployment
