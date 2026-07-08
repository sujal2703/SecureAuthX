# Sprint 11 - Admin Portal

Status: COMPLETED on 2026-07-08

## Objective

Implement an Admin Portal API that allows administrators to view a platform dashboard, browse audit logs, manage system announcements, configure system settings, and track/resolve security incidents.

## Requirements

### Database
- Create V11 Flyway migration with four tables: `audit_logs`, `system_announcements`, `system_settings`, `security_incidents`.
- `audit_logs` columns: id (UUID PK), user_id (FK to users ON DELETE SET NULL), user_email, action (VARCHAR 100), details (TEXT), ip_address, created_at.
- `system_announcements` columns: id (UUID PK), title (VARCHAR 255), content (TEXT), severity (VARCHAR 20, default INFO), active (boolean, default true), created_by (FK to users), expires_at, created_at, updated_at.
- `system_settings` columns: id (UUID PK), key (VARCHAR 255 UNIQUE), value (TEXT), description (VARCHAR 500), created_at, updated_at.
- `security_incidents` columns: id (UUID PK), user_id (FK to users), user_email, incident_type (VARCHAR 100), severity (VARCHAR 20), details, ip_address, resolved (boolean), resolved_at, resolved_by (FK), resolution, created_at.
- Indexes on audit_logs (user_id, action, created_at), security_incidents (user_id), system_settings (key).
- Seed data: 4 default system settings (maintenance_mode, max_login_attempts, session_timeout_minutes, maintenance_message).

### Domain Model
- Entities: `AuditLog`, `SystemAnnouncement`, `SystemSetting`, `SecurityIncident`.
- Repositories with pagination support (`Pageable`), filtering (findByUserId, findByAction, findByResolved, findByActiveTrue, findByExpiresAtAfterOrExpiresAtIsNull).
- DTOs: `AuditLogResponse`, `AnnouncementRequest`, `AnnouncementResponse`, `SystemSettingRequest`, `SystemSettingResponse`, `SecurityIncidentResponse`, `IncidentResolveRequest`, `DashboardResponse`.
- Admin exception: `ResourceNotFoundException` (maps to 404), `IllegalArgumentException` (maps to 400).

### Services
- **AuditService**: records audit entries with action, userId, email, details, ipAddress; retrieves entries with pagination and optional userId/action filtering.
- **DashboardService**: aggregates total users, total/active sessions, total developer projects, pending incidents, recent registrations/logins, recent audit actions from existing repositories.
- **AnnouncementService**: CRUD for announcements with active/expired filtering, created_by tracking.
- **SystemSettingsService**: list/get/update with key-based lookups, seed data fallback.
- **IncidentService**: list with pagination and optional resolved filter, get by id, resolve with admin tracking, create for automated incident recording.

### API
- `GET /api/v1/admin/dashboard` — platform dashboard aggregate.
- `GET /api/v1/admin/audit` — list audit logs (pageable, userId, action filters).
- `GET /api/v1/admin/audit/{id}` — get single audit entry.
- `POST /api/v1/admin/announcements` — create announcement.
- `GET /api/v1/admin/announcements` — list announcements (optional active filter).
- `GET /api/v1/admin/announcements/{id}` — get single announcement.
- `PUT /api/v1/admin/announcements/{id}` — update announcement.
- `DELETE /api/v1/admin/announcements/{id}` — delete announcement.
- `GET /api/v1/admin/settings` — list all settings.
- `GET /api/v1/admin/settings/{key}` — get setting by key.
- `PUT /api/v1/admin/settings/{key}` — update setting value.
- `GET /api/v1/admin/incidents` — list incidents (pageable, resolved filter).
- `GET /api/v1/admin/incidents/{id}` — get single incident.
- `PUT /api/v1/admin/incidents/{id}/resolve` — resolve incident.

### Authorization
- All admin endpoints require `ROLE_ADMIN` via `@PreAuthorize("hasRole('ADMIN')")` and `SecurityConfig.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")`.
- Non-admin users receive `403 Forbidden`.

### Audit Wiring
- Audit events automatically recorded in: `AuthenticationService` (login success, login failed, logout, refresh), `RegistrationService` (registration), `WebAuthnRegistrationService` (passkey registration), `OAuthClientService` (OAuth client created), `SecretRotationService` (secret rotated), `AdminController` (settings update, announcement CRUD, incident resolve).
- `AuditService` injected via `@Autowired(required = false)` to avoid breaking existing tests.
- `IncidentService` injected into `AuthenticationService` to record failed login attempts as security incidents.

### Exception Handling
- `ResourceNotFoundException` → 404 Not Found with `ApiErrorResponse`.
- `IllegalArgumentException` → 400 Bad Request.
- Handlers registered in `GlobalExceptionHandler`.

## Architecture Decisions

### Audit as Passive Service
- `AuditService` uses `@Autowired(required = false)` so existing services and tests without audit beans continue to work unchanged.
- Audit recording is fire-and-forget: failures to write an audit entry do not affect the primary operation.

### Incident Auto-Creation
- Security incidents are automatically created for failed login attempts in `AuthenticationService.login()`.
- This provides out-of-the-box value for security monitoring without manual incident creation.

### Dashboard as Composite Service
- `DashboardService` aggregates data across existing repositories (`UserRepository`, `SessionRepository`, `DeveloperProjectRepository`, `SecurityIncidentRepository`, `AuditLogRepository`) rather than maintaining separate aggregated tables.
- Dashboard data is computed on every request rather than cached. Acceptable for current scale.

### H2 Test Compatibility
- Test profile uses `create-drop` DDL and does not execute Flyway migrations.
- Seed data (system_settings) is inserted in `@BeforeEach` for integration tests.
- Test entities use the same JPA annotations as production without profile-specific configuration.

## Files Changed

### New Files
- `resources/db/migration/V11__Create_admin_portal_tables.sql`
- `admin/entity/AuditLog.java`
- `admin/entity/SystemAnnouncement.java`
- `admin/entity/SystemSetting.java`
- `admin/entity/SecurityIncident.java`
- `admin/repository/AuditLogRepository.java`
- `admin/repository/SystemAnnouncementRepository.java`
- `admin/repository/SystemSettingRepository.java`
- `admin/repository/SecurityIncidentRepository.java`
- `admin/dto/AuditLogResponse.java`
- `admin/dto/AnnouncementRequest.java`
- `admin/dto/AnnouncementResponse.java`
- `admin/dto/SystemSettingRequest.java`
- `admin/dto/SystemSettingResponse.java`
- `admin/dto/SecurityIncidentResponse.java`
- `admin/dto/IncidentResolveRequest.java`
- `admin/dto/DashboardResponse.java`
- `admin/service/AuditService.java`
- `admin/service/DashboardService.java`
- `admin/service/AnnouncementService.java`
- `admin/service/SystemSettingsService.java`
- `admin/service/IncidentService.java`
- `admin/controller/AdminController.java`
- `admin/exception/ResourceNotFoundException.java`
- `admin/service/AuditServiceTests.java`
- `admin/service/DashboardServiceTests.java`
- `admin/service/SystemSettingsServiceTests.java`
- `admin/service/IncidentServiceTests.java`
- `admin/service/AnnouncementServiceTests.java`
- `admin/controller/AdminControllerIntegrationTests.java`

### Modified Files
- `auth/service/AuthenticationService.java`: added AuditService + IncidentService injection
- `auth/service/RegistrationService.java`: added AuditService injection
- `passkey/service/WebAuthnRegistrationService.java`: added AuditService injection
- `oauth/service/OAuthClientService.java`: added AuditService injection, added setHashedClientSecret()
- `developer/service/SecretRotationService.java`: added AuditService injection
- `sessions/repository/SessionRepository.java`: added countByRevokedAtIsNull()
- `config/SecurityConfig.java`: added /api/v1/admin/** hasRole("ADMIN")
- `common/exception/GlobalExceptionHandler.java`: added ResourceNotFoundException + IllegalArgumentException handlers

## Test Coverage

- `AuditServiceTests`: 4 tests (record audit entry, list audit logs, list audit logs with userId filter, get by ID not found)
- `DashboardServiceTests`: 1 test (returns correct aggregated metrics)
- `SystemSettingsServiceTests`: 5 tests (list settings, get setting by key, get setting not found, update setting, update setting not found)
- `IncidentServiceTests`: 4 tests (list all incidents, list unresolved incidents, get by ID, resolve incident, not found)
- `AnnouncementServiceTests`: 7 tests (create, list all, list active only, get by ID, update, delete, not found)
- `AdminControllerIntegrationTests`: 10 integration tests (dashboard, audit list, audit by ID, audit not found, announcements CRUD, settings get/update, incidents list/resolve)

## Verification

- `./gradlew.bat build` succeeds with 209 tests passing (147 pre-existing + 36 developer portal + 26 admin portal).
- All admin endpoints require `ROLE_ADMIN`; non-admin requests return `403 Forbidden`.
- Audit entries are recorded for login, logout, registration, passkey registration, OAuth client creation, and secret rotation.
- Security incidents are auto-created for failed login attempts.
- Dashboard returns correct aggregate metrics.
- System settings are seeded with 4 defaults and can be read/updated.
