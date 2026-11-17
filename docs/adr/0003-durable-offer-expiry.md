# 3. Store offer deadlines in the database

- Status: Accepted
- Date: 2026-11-17

## Context

A waitlist offer is valid for a limited time (by default 15 minutes). When it expires, the
slot must be offered to the next customer. If the deadline only lived in an in-memory timer,
a restart or crash would lose it, and the slot would stay held forever. With more than one
application instance, two timers could also process the same offer.

## Decision

- Each offer has a persisted `expires_at` column. It is the only source of truth.
- Accepting an offer is a single conditional update:

  ```sql
  UPDATE offer
     SET status = 'ACCEPTED', responded_at = now()
   WHERE id = :id AND status = 'PENDING' AND expires_at > now();
  ```

  If no row is updated, the acceptance fails, even if the expiry job has not run yet.
- A scheduled job runs every few seconds and processes overdue offers:

  ```sql
  SELECT id FROM offer
   WHERE status = 'PENDING' AND expires_at <= now()
   ORDER BY expires_at
   LIMIT 50
   FOR UPDATE SKIP LOCKED;
  ```

  Each selected offer becomes `EXPIRED` and the next matching customer receives an offer,
  in the same transaction.

## Alternatives considered

- **In-memory timers** (`ScheduledExecutorService`). Lost on restart. Rejected.
- **Message broker delayed messages.** Adds infrastructure before it is needed, and the
  database would still need to be checked on acceptance. May be revisited later.
- **ShedLock around the job.** Ensures only one instance runs the job, but `SKIP LOCKED`
  already lets several instances share the work safely without an extra table.

## Consequences

- After a restart, the first job run picks up every offer that expired while the app was down.
- Acceptance and expiry can race. Both use `WHERE status = 'PENDING'`, so only one of them
  wins.
- An offer can stay `PENDING` in the table for a few seconds after its deadline, but it can no
  longer be accepted, so this is invisible to users.
- Tests use an injectable `Clock` and simulate a restart by creating a new application
  context against the same database.
