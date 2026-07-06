-- Module: Forgot Password / Email OTP
CREATE TABLE password_reset_otps (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    otp_hash   VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_password_reset_otps_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_otps_user ON password_reset_otps (user_id);
