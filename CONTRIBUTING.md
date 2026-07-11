# Contributing to Campus Connect — Backend

Thanks for your interest in contributing! This document covers everything you need to get set up and submit a change.

## Getting Started

1. Fork the repository and clone your fork:
   ```bash
   git clone https://github.com/<your-username>/campus-connect-backend.git
   cd campus-connect-backend
   ```
2. Create a local PostgreSQL database:
   ```sql
   CREATE DATABASE campusconnect;
   ```
3. Copy the environment template and fill in values:
   ```bash
   cp .env.example .env
   ```
4. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```

Flyway will apply all migrations and seed a bootstrap Super Admin (`SA001` / `ChangeMe123`) on first run against an empty database.

## Branching

Create a feature branch off `main` using a descriptive name:

```bash
git checkout -b feature/short-description
git checkout -b fix/short-description
```

## Making Changes

- Keep pull requests focused on a single feature or fix.
- Follow the existing layered structure: `controller → service (interface) → service/impl → repository → model`, with a DTO at every controller boundary.
- Any new schema change must be a new Flyway migration file (`V{next-number}__description.sql`) under `src/main/resources/db/migration` — never edit an already-applied migration.
- Any new mutating endpoint should call into `AuditLogService` like the existing ones do.
- Add `@PreAuthorize` role checks to new controller endpoints, matching the role that should have access, and validate request DTOs with Bean Validation (`@Valid`).
- Run the build before committing:
  ```bash
  ./mvnw clean compile
  ./mvnw test
  ```

## Commit Messages

Write clear, imperative commit messages, e.g.:

```
Add capacity validation to event registration
Fix refresh token reuse after logout
```

## Submitting a Pull Request

1. Push your branch and open a PR against `main`.
2. Fill out the PR template, describing what changed and why.
3. Link any related issues.
4. Be responsive to review feedback — small, iterative changes are easier to review than large rewrites.

## Load Testing Changes

If your change affects performance-sensitive paths (auth, health check, anything under heavy read/write), consider running the relevant script under `load-tests/` before and after your change:

```bash
k6 run load-tests/health-check-load-test.js
```

## Reporting Bugs & Requesting Features

Please use the issue templates under **Issues → New Issue** so we get the information needed to reproduce or evaluate the request.

## Code of Conduct

This project follows a [Code of Conduct](./CODE_OF_CONDUCT.md). By participating, you agree to uphold it.
