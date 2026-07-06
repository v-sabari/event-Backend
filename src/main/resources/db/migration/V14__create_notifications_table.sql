-- Module: Notifications (in-app)
CREATE TABLE notifications (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    title               VARCHAR(200) NOT NULL,
    message             VARCHAR(1000) NOT NULL,
    type                VARCHAR(50),
    related_entity_type VARCHAR(50),
    related_entity_id   BIGINT,
    read                BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user ON notifications (user_id);
CREATE INDEX idx_notifications_user_read ON notifications (user_id, read);
