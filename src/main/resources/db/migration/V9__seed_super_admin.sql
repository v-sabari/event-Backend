-- Bootstrap seed: without this there is no way to log in on a fresh database
-- (there is intentionally no public self-signup endpoint - see RegisterRequestDTO).
-- Password is 'ChangeMe123' - CHANGE THIS IMMEDIATELY after first login in any
-- non-local environment (use POST /api/auth/forgot-password + OTP flow, or
-- PATCH /api/users/{id}/role via another super admin once one exists).
INSERT INTO users (reg_number, password, name, email, role, enabled, created_at, updated_at)
VALUES (
    'SA001',
    '$2b$10$pWgsh70BySutxdpQOkGM5O1Zbjs04iIgcxpswAGK8he7VS.ALevFe',
    'System Administrator',
    'admin@campusconnect.edu',
    'SUPER_ADMIN',
    TRUE,
    now(),
    now()
);
