-- Module: Winners Management
CREATE TABLE winners (
    id               BIGSERIAL PRIMARY KEY,
    event_id         BIGINT       NOT NULL,
    user_id          BIGINT,
    participant_name VARCHAR(200) NOT NULL,
    position         VARCHAR(50)  NOT NULL,
    prize            VARCHAR(255),
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_winners_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_winners_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_winners_event ON winners (event_id);
