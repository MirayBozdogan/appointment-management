-- btree_gist lets a GiST index combine equality on scalar columns (staff_id)
-- with range overlap (period), which the appointment no-overlap exclusion
-- constraint needs. See docs/adr/0002-db-level-double-booking-prevention.md.
CREATE EXTENSION IF NOT EXISTS btree_gist;
