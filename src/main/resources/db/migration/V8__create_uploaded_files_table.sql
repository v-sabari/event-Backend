-- Module: File Upload Foundation
CREATE TABLE uploaded_files (
    id                 BIGSERIAL PRIMARY KEY,
    original_file_name VARCHAR(255)  NOT NULL,
    stored_file_name   VARCHAR(255)  NOT NULL,
    content_type       VARCHAR(150),
    size_bytes         BIGINT        NOT NULL DEFAULT 0,
    uploaded_by        BIGINT,
    created_at         TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uk_uploaded_files_stored_name UNIQUE (stored_file_name),
    CONSTRAINT fk_uploaded_files_user FOREIGN KEY (uploaded_by) REFERENCES users (id)
);
