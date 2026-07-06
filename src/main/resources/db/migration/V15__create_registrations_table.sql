-- Module: Student Registration + Waiting List + QR Entry Pass + Attendance
CREATE TABLE registrations (
    id             BIGSERIAL PRIMARY KEY,
    event_id       BIGINT       NOT NULL,
    user_id        BIGINT       NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    qr_token       VARCHAR(64)  NOT NULL,
    checked_in_at  TIMESTAMP,
    checked_in_by  BIGINT,
    registered_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_registrations_qr_token UNIQUE (qr_token),
    CONSTRAINT fk_registrations_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_registrations_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_registrations_checked_in_by FOREIGN KEY (checked_in_by) REFERENCES users (id),
    CONSTRAINT chk_registrations_status CHECK (status IN ('REGISTERED', 'WAITLISTED', 'CANCELLED', 'ATTENDED'))
);

-- A student can only have one *active* (non-cancelled) registration per event;
-- re-registering after cancelling is allowed and creates a new row.
CREATE UNIQUE INDEX uk_registrations_active_per_user_event
    ON registrations (event_id, user_id)
    WHERE status <> 'CANCELLED';

CREATE INDEX idx_registrations_event ON registrations (event_id);
CREATE INDEX idx_registrations_user ON registrations (user_id);
CREATE INDEX idx_registrations_event_status ON registrations (event_id, status);
