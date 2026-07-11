# Security Policy

## Supported Versions

This project is under active development on `main`. Security fixes are applied to the latest commit only; there are no maintained release branches at this time.

## Reporting a Vulnerability

If you discover a security vulnerability, please **do not** open a public issue. Instead:

1. Open a [GitHub Security Advisory](../../security/advisories/new) for this repository, or
2. Contact the maintainers directly with details of the issue.

Please include:

- A description of the vulnerability and its potential impact
- Steps to reproduce (proof-of-concept requests, if applicable)
- Any suggested remediation, if known

We'll acknowledge your report as soon as possible and work with you on a fix and coordinated disclosure timeline.

## Notes for This Project

- **`JWT_SECRET` must always be set explicitly.** The application intentionally fails to start if it's missing — there is no baked-in fallback secret, to prevent token forgery in a misconfigured deployment.
- **Change the seeded Super Admin password immediately** (`SA001` / `ChangeMe123`, created by `V9__seed_super_admin.sql`) in any shared or deployed environment, via the forgot-password/OTP flow.
- Login attempts are rate-limited per-account and per-IP (`LoginAttemptServiceImpl`) to mitigate credential stuffing, since `/api/auth/login` is intentionally public.
- File uploads are validated against a content-type allow-list and checked for path traversal (`FileStorageServiceImpl`) before being written to `FILE_UPLOAD_DIR`.
- Deploy behind HTTPS in any real environment — JWTs and passwords must never travel over plain HTTP.
- Set `CORS_ALLOWED_ORIGINS` to your actual frontend domain(s) in production; do not leave it open to all origins.
- Uploaded files, certificates, and gallery images are stored on local disk, not the database — ensure `FILE_UPLOAD_DIR` is on a volume with appropriate access controls and backups.
