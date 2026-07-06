-- Module: Approval History / Reject with Remarks
CREATE TABLE event_approval_history (
    id          BIGSERIAL PRIMARY KEY,
    event_id    BIGINT       NOT NULL,
    from_status VARCHAR(30),
    to_status   VARCHAR(30)  NOT NULL,
    actor_id    BIGINT       NOT NULL,
    remarks     VARCHAR(1000),
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_eah_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_eah_actor FOREIGN KEY (actor_id) REFERENCES users (id)
);

CREATE INDEX idx_eah_event ON event_approval_history (event_id);
