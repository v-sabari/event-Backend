-- Security hardening: refresh tokens were previously persisted as plaintext
-- JWTs. They are now stored as SHA-256 hex digests (see
-- security/RefreshTokenHasher.java), so a leaked DB no longer exposes
-- usable tokens. Pre-existing rows are plaintext JWTs that can never match a
-- hashed lookup and must be removed.
DELETE FROM refresh_tokens;