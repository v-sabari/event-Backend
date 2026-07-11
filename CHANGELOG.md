# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- JWT authentication with access + refresh tokens, BCrypt password hashing, and role-based method security for five roles (`SUPER_ADMIN`, `FACULTY_COORDINATOR`, `HOD`, `STUDENT_ORGANIZER`, `STUDENT`).
- Full event lifecycle: draft → submit → faculty/HOD approval → admin approval → published, with approval history tracking.
- Registrations with capacity/waitlist handling, QR-code entry passes, and QR-based attendance scanning.
- Automatic PDF certificate generation for attendees.
- Venue, department, club, and event-category management.
- Winners, feedback (restricted to attendees), gallery images, and in-app notifications.
- Reports: department-wise/category-wise events, attendance, registrations, feedback analysis, most active department/student.
- Audit logging on every mutating action, queryable by Super Admin.
- Login brute-force protection (per-account and per-IP rate limiting with lockout).
- File upload validation (content-type allow-list, path-traversal guard).
- Flyway-managed PostgreSQL schema (`V1`–`V19`), replacing an earlier MySQL + `ddl-auto=update` setup.
- `/api/health` endpoint for uptime monitoring / keep-warm pinging.
- k6 load-test suite (`load-tests/`) covering baseline load, sustained high concurrency, stress/extreme load, DB connection pooling, and graceful shutdown behavior.

### Changed
- Migrated database engine from MySQL to PostgreSQL to match the intended stack.
- Removed the previously baked-in default JWT signing secret — `JWT_SECRET` is now required with no fallback.

### Fixed
- Duplicate `/api/auth/login` mapping across two controllers.
- Plaintext password storage/comparison in the original login flow.
- Stale duplicate `UserServiceImpl` left in the wrong package after a refactor.
