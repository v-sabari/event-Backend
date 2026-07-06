-- Module: Departments (created first because users.department_id references it)
CREATE TABLE departments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    code        VARCHAR(20)  NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_departments_name UNIQUE (name),
    CONSTRAINT uk_departments_code UNIQUE (code)
);
