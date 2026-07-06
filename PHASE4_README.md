# College Event Management System — Final README (Phase 4)

## 1. Architecture
- **Backend**: Spring Boot 3.2.5 / Java 17, PostgreSQL, Flyway migrations (V1–V19), JWT auth, layered `controller → service → repository → model`.
- **Frontend**: React 19 + Vite + React Router, plain CSS (`App.css`/`Dashboard.css`), axios via `src/services/api.js`.

## 2. Environment Variables (backend)
| Variable | Purpose | Default |
|---|---|---|
| `DB_PASSWORD` | Postgres password | `yourpassword` |
| `JWT_SECRET` | Base64 HMAC key | dev-only default — **override in production** |
| `MAIL_HOST`/`MAIL_PORT`/`MAIL_USERNAME`/`MAIL_PASSWORD`/`MAIL_FROM` | SMTP for OTP/notification emails | Gmail host, empty creds |
| `FILE_UPLOAD_DIR` | Local disk path for uploads/certificates/gallery | `uploads` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origin(s) | `http://localhost:5173` |

## 3. Environment Variables (frontend)
| Variable | Purpose | Default |
|---|---|---|
| `VITE_API_URL` | Backend base URL | `http://localhost:8080` |

Create `event-main/.env` with `VITE_API_URL=http://localhost:8080` (or your deployed backend URL).

## 4. Deployment Guide (summary)
1. Provision PostgreSQL; create an empty `campusconnect` database.
2. Set backend env vars above; run `mvn clean package`; run the jar — Flyway applies V1–V19 automatically, seeding one bootstrap `SUPER_ADMIN` (`SA001` / `ChangeMe123` — **change immediately** via forgot-password/OTP).
3. Set `FILE_UPLOAD_DIR` to a persistent volume/disk in production (uploaded files, certificates, and gallery images are **not** stored in the database).
4. Set real SMTP credentials so OTP/notification emails actually deliver.
5. Set `CORS_ALLOWED_ORIGINS` to your real frontend domain(s).
6. Frontend: `npm install`, set `VITE_API_URL`, `npm run build`, serve the `dist/` output from any static host/CDN behind HTTPS.
7. Put both behind HTTPS (reverse proxy/load balancer) in any real deployment — JWTs and passwords must never travel over plain HTTP.

## 5. Security Checklist
- [x] JWT Authentication (access + refresh tokens, `JwtService`, `JwtAuthenticationFilter`)
- [x] Password Encryption (BCrypt via `SecurityConfig.passwordEncoder()`)
- [x] Role-Based Authorization (`@PreAuthorize` + `@EnableMethodSecurity`, 5 roles)
- [x] Input Validation (Bean Validation `@Valid` on every request DTO)
- [x] File Validation (content-type allow-list + path-traversal guard in `FileStorageServiceImpl`)
- [x] Audit Logs (`AuditLogService`, called from every mutating service)

## 6. Database Tables (actual schema — see mapping notes)
| Requested table | Actual implementation |
|---|---|
| Users | `users` |
| Roles | `Role` Java enum (not a table — small fixed set of 5 values, referenced directly in security rules) |
| Departments | `departments` |
| Clubs | `clubs` |
| Events | `events` |
| Event Approvals | `event_approval_history` |
| Venues | `venues` |
| Registrations | `registrations` |
| Attendance | embedded in `registrations` (`status=ATTENDED`, `checked_in_at`, `checked_in_by`) rather than a separate table, since attendance is 1:1 with a registration |
| Certificates | `certificates` |
| Winners | `winners` |
| Feedback | `feedback` |
| Notifications | `notifications` |
| Gallery | `gallery_images` (+ reuses `uploaded_files`) |
| Audit Logs | `audit_logs` |

Plus supporting tables not in the original list but required by earlier phases: `refresh_tokens`, `password_reset_otps`, `event_categories`, `uploaded_files`.

## 7. Testing Checklist
- [ ] `mvn clean compile` / `mvn test` on the backend (this sandbox has no Maven Central access — verified statically instead, see below)
- [ ] `npm install && npm run build` on the frontend
- [ ] Login with seed admin → create department/venue/category/club → register a Faculty Coordinator, Student Organizer, Student
- [ ] Organizer creates draft → submits → Faculty approves → (HOD if department requires it) → Admin approves → event PUBLISHED
- [ ] Student registers → capacity/waitlist behavior when event is full → cancel → waitlist promotion
- [ ] QR entry pass downloads and scans correctly via `/api/attendance/scan`
- [ ] Certificate generates only after `ATTENDED` status; PDF downloads
- [ ] Feedback only accepted from attendees, one per student per event
- [ ] Winners and gallery images attach correctly to an event
- [ ] Notifications appear in-app and (with real SMTP) by email
- [ ] Admin/Organizer/Student dashboards show correct aggregate numbers
- [ ] Reports return correct department/category/attendance/feedback figures
- [ ] Search & Filters return correct results for each filter combination

## 8. Bug Checklist (fixed across phases — kept for traceability)
- [x] Duplicate `/api/auth/login` mapping (`AuthController` + `UserController`) — Phase 2
- [x] Plaintext password storage/comparison — Phase 2
- [x] Missing JWT issuance on login — Phase 2
- [x] Missing `axios` imports in `Login.jsx`/`StudentDashboard.jsx`/`FacultyDashboard.jsx` — Phase 3
- [x] Empty `services/api.js` — Phase 3
- [x] Dead `EventCard` Register button — Phase 3
- [x] `FacultyDashboard` sidebar links pointing to non-existent sections — Phase 3
- [x] Stale duplicate `UserServiceImpl` left in wrong package — Phase 3 (caught during continuation)
- [x] MySQL vs stated PostgreSQL stack mismatch — Phase 2 (migrated to Postgres + Flyway)

## 9. Production Readiness Notes
- Reports are computed via in-memory aggregation over existing repositories — fine at college-event scale; if data volume grows significantly, migrate to DB-side `GROUP BY` queries.
- `spring.jpa.open-in-view` is left at Spring Boot's default (`true`) — lazy associations load transparently through the controller layer; consider disabling and adding explicit fetch joins if moving to a stricter architecture later.
- File storage is local disk (`FileStorageServiceImpl`); swap for S3/GCS by re-implementing `FileStorageService` only — nothing else references the filesystem directly.
- No automated test suite was added in this pass (out of scope given time); the Testing Checklist above should be run manually before go-live, and Postman collections (`postman/`) support that.

## 10. API Documentation
No separate OpenAPI/Swagger UI is wired in. API surface is fully documented via:
- `postman/Phase2_Backend_Foundation.postman_collection.json` (auth, users, departments, clubs, categories, audit logs, files)
- `postman/Phase3_4_Full_Collection.postman_collection.json` (events, approval workflow, venues, registrations, QR/attendance, certificates, feedback, winners, gallery, notifications, dashboards, reports, search)

Every endpoint follows the `ApiResponse<T>` / `ErrorResponse` envelope defined in `dto/common/`.
