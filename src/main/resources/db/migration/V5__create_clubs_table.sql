-- Module: Clubs
CREATE TABLE clubs (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(150)  NOT NULL,
    description    VARCHAR(1000),
    department_id  BIGINT,
    coordinator_id BIGINT,
    active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uk_clubs_name UNIQUE (name),
    CONSTRAINT fk_clubs_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_clubs_coordinator FOREIGN KEY (coordinator_id) REFERENCES users (id)
);

CREATE INDEX idx_clubs_department ON clubs (department_id);
CREATE INDEX idx_clubs_coordinator ON clubs (coordinator_id);
