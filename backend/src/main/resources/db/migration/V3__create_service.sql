CREATE TABLE service (
    id               uuid        PRIMARY KEY,
    business_id      uuid        NOT NULL REFERENCES business (id),
    name             text        NOT NULL CHECK (length(trim(name)) > 0),
    duration_minutes integer     NOT NULL CHECK (duration_minutes BETWEEN 5 AND 600),
    price_cents      integer     NOT NULL CHECK (price_cents >= 0),
    active           boolean     NOT NULL DEFAULT true,
    created_at       timestamptz NOT NULL DEFAULT now()
);

-- Service names are unique within a business, ignoring case.
-- The index also serves lookups by business_id.
CREATE UNIQUE INDEX service_business_name_uq ON service (business_id, lower(name));
