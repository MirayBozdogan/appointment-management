# 5. Use Spring Data JPA, with explicit SQL where it matters

- Status: Accepted
- Date: 2026-11-19

## Context

The backend needs a persistence approach for ordinary CRUD (businesses, services, staff) and
for a few critical, concurrency-sensitive operations (booking, offer acceptance, offer expiry).
PostgreSQL range types (`tstzrange`) are not mapped by JPA out of the box.

## Decision

- Use **Spring Data JPA** (Hibernate) for entities and repositories.
- **Flyway owns the schema.** Hibernate runs with `ddl-auto: validate` and never changes tables.
- `open-in-view` is disabled, so database access happens only inside service methods.
- Appointments store `start_at` and `end_at` as plain `timestamptz` columns. The `period`
  range used by the exclusion constraint is a **generated column**:

  ```sql
  period tstzrange GENERATED ALWAYS AS (tstzrange(start_at, end_at, '[)')) STORED
  ```

  Java never reads or writes `period`, so no custom Hibernate type is needed.
- Critical state transitions (accepting or expiring an offer, picking overdue offers with
  `FOR UPDATE SKIP LOCKED`) are written as explicit SQL / `@Modifying` queries, not as
  "load entity, change field, flush".

## Alternatives considered

- **Spring Data JDBC.** Simpler, no lazy loading or dirty checking. Less common in the job
  market and in existing codebases this project aims to resemble.
- **jOOQ.** Excellent for SQL-heavy code, but adds code generation and a commercial licence
  question for some databases. More than this project needs.
- **Custom Hibernate type for `tstzrange`.** Works, but adds a dependency for a column the
  application never needs to read.

## Consequences

- Most code is ordinary JPA that any Spring developer can read.
- The rules that protect correctness are visible as SQL in migrations and repository queries.
- Lazy loading must be kept in mind: services return records (`...View`), never entities.
