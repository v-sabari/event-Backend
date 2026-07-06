-- Module: Event Creation + Approval Workflow
CREATE TABLE events (
    id                    BIGSERIAL PRIMARY KEY,
    title                 VARCHAR(200)  NOT NULL,
    description           VARCHAR(2000),
    category_id           BIGINT,
    club_id               BIGINT,
    department_id         BIGINT,
    venue_id              BIGINT        NOT NULL,
    start_time            TIMESTAMP     NOT NULL,
    end_time              TIMESTAMP     NOT NULL,
    registration_deadline TIMESTAMP     NOT NULL,
    max_participants      INT           NOT NULL,
    fee                   NUMERIC(10,2) NOT NULL DEFAULT 0,
    banner_url            VARCHAR(500),
    status                VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    created_by            BIGINT        NOT NULL,
    created_at            TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT fk_events_category   FOREIGN KEY (category_id)   REFERENCES event_categories (id),
    CONSTRAINT fk_events_club       FOREIGN KEY (club_id)       REFERENCES clubs (id),
    CONSTRAINT fk_events_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_events_venue      FOREIGN KEY (venue_id)      REFERENCES venues (id),
    CONSTRAINT fk_events_created_by FOREIGN KEY (created_by)    REFERENCES users (id),
    CONSTRAINT chk_events_status CHECK (status IN (
        'DRAFT', 'PENDING_FACULTY_APPROVAL', 'PENDING_HOD_APPROVAL', 'PENDING_ADMIN_APPROVAL',
        'REJECTED', 'PUBLISHED', 'CANCELLED', 'COMPLETED'
    )),
    CONSTRAINT chk_events_time CHECK (end_time > start_time),
    CONSTRAINT chk_events_deadline CHECK (registration_deadline <= start_time),
    CONSTRAINT chk_events_capacity CHECK (max_participants > 0)
);

CREATE INDEX idx_events_status ON events (status);
CREATE INDEX idx_events_venue ON events (venue_id);
CREATE INDEX idx_events_created_by ON events (created_by);
CREATE INDEX idx_events_start_time ON events (start_time);
