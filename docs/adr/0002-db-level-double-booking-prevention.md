# 2. Prevent double booking in the database

- Status: Accepted
- Date: 2026-11-17

## Context

Two customers can try to book the same staff member at the same time. A check in
application code ("is this slot free?" followed by "insert") has a race window: both
requests can pass the check before either one inserts. The same race exists between a
regular booking and a waitlist offer being accepted.

## Decision

Store each appointment's time as a `tstzrange` and add a PostgreSQL exclusion constraint:

```sql
EXCLUDE USING gist (staff_id WITH =, period WITH &&)
WHERE (status IN ('BOOKED', 'HELD'))
```

The database rejects any active appointment that overlaps another active appointment of the
same staff member, regardless of how many application instances or threads are running.
The application translates the constraint violation into a `409 Conflict` response.

A slot being offered to the waitlist is stored as a `HELD` appointment, so it is protected
by the same constraint.

## Alternatives considered

- **Application check only.** Has the race described above. Rejected.
- **`SELECT ... FOR UPDATE` on existing rows.** Does not lock rows that do not exist yet,
  so two inserts into an empty slot can still both succeed. Rejected.
- **`SERIALIZABLE` isolation.** Works, but every booking transaction must handle retries,
  and the rule is hidden in code instead of being visible in the schema. Rejected.
- **Fixed time slots with a unique index** on `(staff_id, start_time)`. Simple, but only
  works if all services have the same length. Rejected.

## Consequences

- Correctness does not depend on every code path remembering to check.
- Requires the `btree_gist` extension.
- The application still checks availability first to give a friendly error. The
  constraint is the final guarantee.
- A Testcontainers test starts many concurrent bookings for the same slot and asserts that
  exactly one succeeds.
