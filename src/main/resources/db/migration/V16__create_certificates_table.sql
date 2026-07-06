-- Module: Certificate Generation
CREATE TABLE certificates (
    id                BIGSERIAL PRIMARY KEY,
    registration_id   BIGINT       NOT NULL,
    stored_file_name  VARCHAR(255) NOT NULL,
    certificate_code  VARCHAR(40)  NOT NULL,
    generated_at      TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_certificates_registration UNIQUE (registration_id),
    CONSTRAINT uk_certificates_code UNIQUE (certificate_code),
    CONSTRAINT fk_certificates_registration FOREIGN KEY (registration_id) REFERENCES registrations (id) ON DELETE CASCADE
);
