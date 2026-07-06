-- Module: User Roles + core identity
-- Fresh table definition (project is moving from MySQL/ddl-auto to a
-- Flyway-managed PostgreSQL schema, so there is no legacy data to preserve).
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    reg_number    VARCHAR(50)  NOT NULL,
    password      VARCHAR(255) NOT NULL,
    name          VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    department_id BIGINT,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_reg_number UNIQUE (reg_number),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT chk_users_role CHECK (role IN ('SUPER_ADMIN', 'FACULTY_COORDINATOR', 'STUDENT_ORGANIZER', 'STUDENT'))
);

CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_department ON users (department_id);
