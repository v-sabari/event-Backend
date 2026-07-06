-- Module: Venue Management
CREATE TABLE venues (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    location   VARCHAR(255),
    capacity   INT          NOT NULL DEFAULT 0,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_venues_name UNIQUE (name),
    CONSTRAINT chk_venues_capacity CHECK (capacity >= 0)
);
