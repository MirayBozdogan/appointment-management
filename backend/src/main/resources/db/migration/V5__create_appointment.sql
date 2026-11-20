CREATE TABLE appointment (
    id           uuid        PRIMARY KEY,
    business_id  uuid        NOT NULL REFERENCES business (id),
    staff_id     uuid        NOT NULL REFERENCES staff_member (id),
    service_id   uuid        NOT NULL REFERENCES service (id),
    -- Temporary: sent by the client until authentication exists. A foreign key to the
    -- users table will be added together with the identity module.
    customer_id  uuid,
    start_at     timestamptz NOT NULL,
    end_at       timestamptz NOT NULL,
    -- Derived from start_at/end_at and used only by the no-overlap constraint (ADR 0005).
    period       tstzrange   GENERATED ALWAYS AS (tstzrange(start_at, end_at, '[)')) STORED,
    status       text        NOT NULL
                             CHECK (status IN ('BOOKED', 'HELD', 'CANCELLED', 'COMPLETED', 'RELEASED')),
    version      bigint      NOT NULL DEFAULT 0,
    created_at   timestamptz NOT NULL DEFAULT now(),
    cancelled_at timestamptz,

    CHECK (start_at < end_at),

    -- A HELD slot (offered to the waitlist) or a RELEASED one belongs to no customer;
    -- every other status must have one.
    CONSTRAINT appointment_customer_matches_status
        CHECK ((status IN ('HELD', 'RELEASED')) = (customer_id IS NULL)),

    -- The core rule of the project: active appointments of one staff member never overlap.
    -- See docs/adr/0002-db-level-double-booking-prevention.md.
    CONSTRAINT appointment_no_overlap
        EXCLUDE USING gist (staff_id WITH =, period WITH &&)
        WHERE (status IN ('BOOKED', 'HELD'))
);

CREATE INDEX appointment_business_start_idx ON appointment (business_id, start_at);
CREATE INDEX appointment_customer_idx ON appointment (customer_id);
