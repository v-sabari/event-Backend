-- Module: Feedback
CREATE TABLE feedback (
    id         BIGSERIAL PRIMARY KEY,
    event_id   BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    rating     INT          NOT NULL,
    comments   VARCHAR(2000),
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_feedback_event_user UNIQUE (event_id, user_id),
    CONSTRAINT fk_feedback_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_feedback_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_feedback_event ON feedback (event_id);
