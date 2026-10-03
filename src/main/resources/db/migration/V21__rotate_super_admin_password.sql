-- Rotates the SA001 bootstrap password that V9 seeded as 'ChangeMe123'.
-- That value was documented in the README and STILL ACTIVE on the live
-- production deployment (verified 2026-10-03: /api/auth/login returned a
-- SUPER_ADMIN token), meaning anyone who read the repo could take over the
-- system as Super Admin.
--
-- This migration writes the bcrypt hash of a freshly generated, strong random
-- 24-character password. The raw value was handed to the repository owner
-- out-of-band (chat/secret manager) and is NOT stored anywhere in this repo.
-- Brute-forcing it from this cost-10 hash is not feasible.
--
-- Follow-up: once real SMTP is configured (spring.mail.* in application.properties)
-- and SA001's email is a real, controlled inbox, rotate again via the
-- forgot-password/OTP flow so the password is under the owner's direct control.
UPDATE users
SET password = '$2b$10$oYp11rE/arSzU1bXpy4Z9uFbcaX3VHvyzY2OrtUSHVZbMJlWufcRG',
    updated_at = now()
WHERE reg_number = 'SA001'
  AND role = 'SUPER_ADMIN';