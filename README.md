# Campus Connect — Backend 🎓⚙️

Spring Boot REST API powering **Campus Connect**, a role-based event management platform for colleges and universities. Handles auth, event lifecycle & approvals, registrations, QR-based attendance, PDF certificates, venues/departments/clubs, reports, notifications, and audit logging.

> This repository is the **backend**. The companion React/Vite frontend lives in a separate repo — see [Related Repositories](#related-repositories).

---

## Table of Contents

- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Roles & Security](#roles--security)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Database & Migrations](#database--migrations)
- [Demo / Seed Credentials](#demo--seed-credentials)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Testing & Load Testing](#testing--load-testing)
- [Docker](#docker)
- [Deployment](#deployment)
- [Related Repositories](#related-repositories)
- [Contributing](#contributing)
- [License](#license)

---

## Architecture

Layered architecture: `controller → service (+ impl) → repository → model`, with DTOs at every controller boundary and a consistent `ApiResponse<T>` / `ErrorResponse` envelope for all responses.

- **Framework:** Spring Boot 3.2.5, Java 17
- **Database:** PostgreSQL, schema owned by **Flyway** migrations (`V1`–`V19`)
- **Auth:** JWT (access + refresh tokens), BCrypt password hashing, role-based method security
- **Files:** local disk storage (uploads, certificates, gallery images) via a swappable `FileStorageService`

## Tech Stack

| Concern | Technology |
|---|---|
| Web | Spring Boot Starter Web |
| Persistence | Spring Data JPA + PostgreSQL |
| Migrations | Flyway |
| Security | Spring Security + JWT (`jjwt`) |
| Validation | Bean Validation (`@Valid`) |
| QR codes | ZXing |
| PDF certificates | Apache PDFBox |
| Email | Spring Boot Starter Mail (OTP delivery) |
| Boilerplate | Lombok |
| Load testing | [k6](https://k6.io/) |

## Roles & Security

Five roles, enforced with `@EnableMethodSecurity` + `@PreAuthorize` on every controller: `SUPER_ADMIN`, `FACULTY_COORDINATOR`, `HOD`, `STUDENT_ORGANIZER`, `STUDENT`.

Security measures in place:

- ✅ JWT access + refresh tokens (`JwtService`, `JwtAuthenticationFilter`)
- ✅ BCrypt password hashing (`SecurityConfig.passwordEncoder()`)
- ✅ Role-based authorization on every endpoint
- ✅ Request body validation on every DTO
- ✅ File upload validation — content-type allow-list + path-traversal guard (`FileStorageServiceImpl`)
- ✅ Login brute-force protection — per-account **and** per-IP rate limiting with temporary lockout (`LoginAttemptServiceImpl`)
- ✅ Audit logging on every mutating action (`AuditLogService`, queryable via `GET /api/audit-logs`, Super Admin only)
- ✅ No hardcoded JWT signing secret — the app refuses to start without `JWT_SECRET` set explicitly

## Getting Started

### Prerequisites

- Java 17
- Maven (or use the bundled `./mvnw` / `mvnw.cmd`)
- PostgreSQL running locally (or accessible remotely)

### 1. Create the database

```sql
CREATE DATABASE campusconnect;
```

### 2. Configure environment variables

Copy `.env.example` to `.env` (or export the variables directly) — see [Environment Variables](#environment-variables) below for the full list and defaults.

### 3. Run the app

```bash
./mvnw spring-boot:run
```

On first run against an empty database, Flyway automatically applies all migrations (`V1`–`V19`) and seeds one bootstrap Super Admin account (see [Demo / Seed Credentials](#demo--seed-credentials)).

The API will be available at `http://localhost:8080`. Health check: `GET /api/health` (also aliased at `/health`) — used by uptime monitors to keep a free-tier deployment warm.

## Environment Variables

| Variable | Purpose | Default |
|---|---|---|
| `DB_URL` | Full JDBC URL | `jdbc:postgresql://localhost:5432/campusconnect` |
| `DB_USERNAME` | Postgres username | *(required)* |
| `DB_PASSWORD` | Postgres password | *(required)* |
| `JWT_SECRET` | Base64-encoded HMAC signing key for JWTs | *(required — app fails to start if unset)* |
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | SMTP username | *(empty)* |
| `MAIL_PASSWORD` | SMTP password / app password | *(empty)* |
| `MAIL_FROM` | From-address for OTP/notification emails | `noreply@campusconnect.edu` |
| `FILE_UPLOAD_DIR` | Local folder for uploads/certificates/gallery | `uploads` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allow-list for frontend origin(s) | `http://localhost:5173` |

Generate a `JWT_SECRET` locally with:

```bash
openssl rand -base64 64
```

> ⚠️ Without real SMTP credentials, OTP emails won't actually deliver, but `forgot-password` / `verify-otp` / `reset-password` still work end-to-end (the OTP is generated and stored regardless — delivery failure is logged, not thrown).

## Database & Migrations

Schema is fully owned by Flyway (`src/main/resources/db/migration/V1__...` through `V19__...`); `spring.jpa.hibernate.ddl-auto` is set to `validate`, so the app **will not start** if the JPA entities and the migrated schema disagree.

| Table | Notes |
|---|---|
| `users`, `departments`, `clubs`, `events`, `venues` | Core entities |
| `event_categories`, `event_approval_history` | Event workflow support |
| `registrations` | Also models attendance (`status=ATTENDED`, `checked_in_at`, `checked_in_by`) rather than a separate table |
| `certificates`, `winners`, `feedback`, `notifications` | Post-event features |
| `gallery_images`, `uploaded_files` | File-backed content |
| `audit_logs` | Full mutation audit trail |
| `refresh_tokens`, `password_reset_otps` | Auth support tables |

`Role` is a fixed 5-value Java enum, not a database table, since the role set is small and referenced directly in security rules.

## Demo / Seed Credentials

A Flyway seed migration (`V9__seed_super_admin.sql`) creates one bootstrap account on a fresh database so you can log in immediately:

| Field | Value |
|---|---|
| Registration number | `SA001` |
| Password | `ChangeMe123` |
| Role | `SUPER_ADMIN` |

> ⚠️ **Change this password immediately** in any shared or deployed environment, via the forgot-password/OTP flow. There is intentionally no public self-signup endpoint — all other accounts are created by an admin or faculty coordinator via `POST /api/users`.

## API Documentation

No OpenAPI/Swagger UI is currently wired in. The full API surface is documented via Postman collections (add these under a `postman/` folder if not already present in your working copy):

- `Phase2_Backend_Foundation.postman_collection.json` — auth, users, departments, clubs, categories, audit logs, files
- `Phase3_4_Full_Collection.postman_collection.json` — events, approval workflow, venues, registrations, QR/attendance, certificates, feedback, winners, gallery, notifications, dashboards, reports, search

Every endpoint returns the same `ApiResponse<T>` / `ErrorResponse` envelope (`dto/common/`).

## Project Structure

```
src/main/java/com/example/Backend/
├── controller/        # REST endpoints (Auth, Event, Registration, Attendance, Certificate, Venue, ...)
├── service/            # Interfaces
│   └── impl/            # Implementations
├── repository/         # Spring Data JPA repositories
├── model/               # JPA entities
├── dto/                 # Request/response DTOs, grouped by feature
├── security/            # JwtService, JwtAuthenticationFilter
├── config/               # SecurityConfig, etc.
├── exception/            # Custom exceptions + GlobalExceptionHandler
└── util/                  # OtpGenerator, etc.

src/main/resources/
├── application.properties
└── db/migration/         # Flyway migrations V1–V19
```

## Testing & Load Testing

Automated unit/integration test coverage is minimal at this stage (see [Testing Checklist](#) in project docs) — contributions here are very welcome.

Performance and resilience are exercised with [k6](https://k6.io/) scripts against `GET /api/health`:

| Script | Purpose |
|---|---|
| `load-tests/load-test.js` | Baseline load ramp (10 → 50 VUs) |
| `load-tests/health-check-load-test.js` | Sustained high-concurrency health check (up to 200 VUs) with strict latency thresholds |
| `load-tests/stress-test.js` | Stress/extreme ramp up to 300 VUs to find breaking points |
| `load-tests/db-connection-test.js` | Ramps load while exercising multiple endpoints to stress DB connection pooling |
| `load-tests/graceful-shutdown-test.js` | Sustains load while the backend is killed mid-test, to verify graceful shutdown behavior |

Run any script with:

```bash
k6 run load-tests/health-check-load-test.js
```

## Docker

Build and run with the included multi-stage `Dockerfile`:

```bash
docker build -t campus-connect-backend .
docker run -p 8080:8080 --env-file .env campus-connect-backend
```

## Deployment

1. Provision PostgreSQL and create an empty `campusconnect` database.
2. Set all required environment variables (see above) on your host/platform.
3. Point `FILE_UPLOAD_DIR` at a **persistent volume** — uploaded files, certificates, and gallery images are stored on disk, not in the database.
4. Set real SMTP credentials so OTP/notification emails deliver.
5. Set `CORS_ALLOWED_ORIGINS` to your real frontend domain(s).
6. Put the service behind HTTPS (reverse proxy / load balancer) — JWTs and passwords must never travel over plain HTTP.
7. Change the seeded `SA001` password immediately after first login.

The health endpoint (`/api/health`) is designed to be pinged by an uptime monitor to keep free-tier hosting (e.g. Render) warm.

## Related Repositories

- **Frontend:** React + Vite client — see the `event` repository ([live demo](https://event-two-ivory.vercel.app/)).

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](./CONTRIBUTING.md) before opening a pull request, and note that this project follows a [Code of Conduct](./CODE_OF_CONDUCT.md).

## License

This project is licensed under the [MIT License](./LICENSE).
