CREATE TABLE business (
    id                uuid        PRIMARY KEY,
    name              text        NOT NULL CHECK (length(trim(name)) > 0),
    slug              text        NOT NULL UNIQUE,
    timezone          text        NOT NULL,
    offer_ttl_minutes integer     NOT NULL DEFAULT 15 CHECK (offer_ttl_minutes BETWEEN 1 AND 1440),
    created_at        timestamptz NOT NULL DEFAULT now()
);
