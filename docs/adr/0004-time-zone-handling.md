# 4. Time zone handling

- Status: Accepted
- Date: 2026-11-17

## Context

Businesses, customers and servers can be in different time zones. Working hours such as
"Monday 09:00–18:00" mean local wall-clock time and must stay correct across daylight saving
time changes. Appointments, on the other hand, are exact moments in time.

## Decision

- **Instants** (appointments, offers, audit events) are stored as `timestamptz` and handled as
  `Instant` / `OffsetDateTime` in Java. The JDBC session and the JVM run in UTC.
- **Working hours** are stored as local `time` values plus a `day_of_week`, without a zone.
- **Each business** has an IANA time zone (for example `Europe/Istanbul`) in `business.timezone`.
- **Availability** is computed by combining a date, a local working-hours window and the
  business zone into a `ZonedDateTime`, then converting it to an `Instant`.
- **The API** returns instants in ISO-8601 with an offset, plus the business time zone.
  The frontend displays times in the business's zone, not the browser's.

## Alternatives considered

- **Store working hours in UTC.** Breaks twice a year in zones with daylight saving time
  (09:00 local would become 08:00 or 10:00). Rejected.
- **Store local date-times without a zone for appointments.** Ambiguous during DST changes
  and impossible to compare across businesses. Rejected.

## Consequences

- Availability tests cover a day with a DST transition, using a zone that has one
  (for example `Europe/Berlin`), even though `Europe/Istanbul` currently does not.
- A customer in another zone sees the business's local time, which matches what they will
  experience when they arrive.
