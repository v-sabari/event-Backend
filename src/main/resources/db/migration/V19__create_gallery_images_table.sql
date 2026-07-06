-- Module: Gallery (links File Upload Foundation files to an event)
CREATE TABLE gallery_images (
    id               BIGSERIAL PRIMARY KEY,
    event_id         BIGINT       NOT NULL,
    uploaded_file_id BIGINT       NOT NULL,
    caption          VARCHAR(255),
    uploaded_by      BIGINT       NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_gallery_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_gallery_uploaded_file FOREIGN KEY (uploaded_file_id) REFERENCES uploaded_files (id),
    CONSTRAINT fk_gallery_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id)
);

CREATE INDEX idx_gallery_event ON gallery_images (event_id);
