-- Module: Audit Logs
CREATE TABLE audit_logs (
    id           BIGSERIAL PRIMARY KEY,
    performed_by VARCHAR(100),
    action       VARCHAR(100)  NOT NULL,
    entity_type  VARCHAR(100),
    entity_id    BIGINT,
    details      VARCHAR(1000),
    created_at   TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_performed_by ON audit_logs (performed_by);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs (entity_type);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
