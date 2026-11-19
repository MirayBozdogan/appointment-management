CREATE TABLE staff_member (
    id           uuid        PRIMARY KEY,
    business_id  uuid        NOT NULL REFERENCES business (id),
    user_id      uuid,       -- linked to a user account once the identity module exists
    display_name text        NOT NULL CHECK (length(trim(display_name)) > 0),
    active       boolean     NOT NULL DEFAULT true,
    created_at   timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX staff_member_business_idx ON staff_member (business_id);

-- Which services a staff member performs.
CREATE TABLE staff_service (
    staff_id   uuid NOT NULL REFERENCES staff_member (id) ON DELETE CASCADE,
    service_id uuid NOT NULL REFERENCES service (id),
    PRIMARY KEY (staff_id, service_id)
);

-- Weekly working hours in the business's LOCAL time (see ADR 0004).
-- day_of_week follows ISO-8601: 1 = Monday ... 7 = Sunday.
CREATE TABLE working_hours (
    staff_id    uuid    NOT NULL REFERENCES staff_member (id) ON DELETE CASCADE,
    day_of_week integer NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_local time    NOT NULL,
    end_local   time    NOT NULL,
    CHECK (start_local < end_local),
    -- Intervals of the same staff member on the same day must not overlap.
    -- PostgreSQL has no time range type, so times are placed on a fixed date.
    CONSTRAINT working_hours_no_overlap EXCLUDE USING gist (
        staff_id WITH =,
        day_of_week WITH =,
        tsrange(DATE '2000-01-01' + start_local, DATE '2000-01-01' + end_local) WITH &&
    )
);
