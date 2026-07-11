# Phase 2 — Backend Foundation Setup Notes

## What changed at the infrastructure level
- **Database engine**: switched from MySQL to **PostgreSQL** to match the originally stated stack. Update `spring.datasource.*` in `application.properties` (or the `DB_PASSWORD` env var) to point at your local Postgres instance and create an empty `campusconnect` database.
- **Schema ownership**: moved from `ddl-auto=update` to **Flyway migrations** (`src/main/resources/db/migration/V1..V9`). `ddl-auto` is now `validate` — Hibernate will refuse to start if the entities and the migrated schema disagree, which is what you want.
- Run the app once against an empty database and Flyway will apply all 9 migrations in order automatically.

## Required environment variables (all have safe local-dev defaults except mail)
| Variable | Purpose | Local default |
|---|---|---|
| `DB_PASSWORD` | Postgres password | `yourpassword` |
| `JWT_SECRET` | Base64-encoded HMAC signing key for JWTs | dev-only default baked in — **override in any shared/deployed environment** |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP credentials for OTP emails | Gmail SMTP host/port, empty credentials |
| `MAIL_FROM` | From-address for OTP emails | `noreply@campusconnect.edu` |
| `FILE_UPLOAD_DIR` | Local folder for uploaded files | `uploads` (relative to working dir) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allow-list for the frontend origin(s) | `http://localhost:5173` |

Without real SMTP credentials, `POST /api/auth/forgot-password` will still generate and store an OTP (so `verify-otp`/`reset-password` work), it just won't successfully deliver the email — this is logged, not thrown, so the endpoint still responds normally.

## Bootstrap login (seeded by V9__seed_super_admin.sql)
There is intentionally no public self-signup endpoint (account creation is admin/faculty-coordinator driven via `POST /api/users`). A seed migration creates one starting account so you can log in at all on a fresh database:

```
regNumber: SA001
password:  ChangeMe123
role:      SUPER_ADMIN
```

**Change this password immediately** in any environment beyond your own machine, via the forgot-password/OTP flow.

## Quick smoke-test order (also encoded in the Postman collection)
1. `POST /api/auth/login` with the seed admin → copy `accessToken`/`refreshToken`.
2. `POST /api/departments` (as SUPER_ADMIN) → create at least one department.
3. `POST /api/users` → register a STUDENT/FACULTY_COORDINATOR/STUDENT_ORGANIZER account against that department.
4. `POST /api/clubs`, `POST /api/event-categories` → master data.
5. `GET /api/audit-logs` (as SUPER_ADMIN) → confirm every action above was recorded.
6. `POST /api/files/upload` (multipart, key `file`) → confirm it returns a `downloadUrl`, then `GET` that URL with no auth header (it's public by design).

## Postman collection
`postman/Phase2_Backend_Foundation.postman_collection.json` — import into Postman. It auto-captures `accessToken`/`refreshToken`/`departmentId`/`clubId`/`categoryId`/`userId` into collection variables via test scripts, so requests can be run top-to-bottom without manual copy-pasting.

## Known limitation of this verification pass
This environment's outbound network is restricted to package registries needed for *this* conversation (npm/pypi/etc.) and does not include Maven Central, so `mvn compile`/`mvn test` could not be executed live here. Verification was done by:
- Static resolution of every internal `import com.example.Backend.*` against the actual file tree (all resolve).
- Cross-checking every DTO getter/field used in services against the DTO's actual declared fields.
- Cross-checking every `@Value("${...}")` property against `application.properties`.
- Validating `pom.xml` as well-formed XML and every migration file for balanced syntax.
- Manual review of annotation/type correctness (JPA mappings, `@PreAuthorize` expressions, Spring Security wiring).

**Please run `mvn clean compile` (or open the project in your IDE) as the final check on your machine before relying on this in a real environment** — that's the one verification step this sandbox couldn't do for you.

## Bug fixed along the way
While rebuilding `UserServiceImpl`, an old duplicate copy was left behind at `service/UserServiceImpl.java` (the class had been moved to `service/impl/UserServiceImpl.java`). It was still using plaintext password comparison and the old `Map<String,Object>` login return type, and would not have compiled against the updated `UserService` interface. It has been deleted — `service/impl/UserServiceImpl.java` is the only implementation now.

## What's deliberately NOT done yet
Per your instruction, **Event Management is untouched** — `EventController.java` remains the original empty stub. Nothing in Phase 2 references an Event entity yet (Departments/Clubs/EventCategories are all standalone master data, ready for the Event entity to reference once you give the go-ahead).
