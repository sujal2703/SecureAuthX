# SecureAuthX PRD

## Current Product Stage

SecureAuthX is in Sprint 11: Admin Portal.

The current product objective is implementing an Admin Portal API that allows administrators to view a platform dashboard, browse audit logs, manage system announcements, configure system settings, and track/resolve security incidents.

## Sprint 11 Scope

In scope:

- Platform dashboard with aggregate metrics (total users, sessions, active apps, pending incidents).
- Audit log browsing with pagination and filtering (by userId, action).
- Automated audit recording for login/logout/registration/passkey/oauth/secret-rotation/admin actions.
- System announcement CRUD (create, list, get, update, delete) with severity levels and expiry.
- System settings management (list, get by key, update value) — seeded with 4 default settings.
- Security incident tracking (list with pagination and resolved filter, get by id, resolve).
- Automated incident creation for failed login attempts.
- Admin-only access via `ROLE_ADMIN` and `@PreAuthorize("hasRole('ADMIN')")`.
- All 209 tests pass (183 pre-existing + 26 new admin portal tests).

Out of scope:

- Frontend/UI for the admin portal (backend API only).
- Real-time alerts or WebSocket-based notifications.
- Incident severity auto-escalation.
- Audit log retention/cleanup policies.
- Export/download of audit logs.
- Multi-admin audit (who performed which action is tracked).

## Completed Sprints

- **Sprint 00**: Project foundation — Docker, PostgreSQL, Redis, Flyway, OpenAPI, health checks.
- **Sprint 01**: User registration — email/password validation, Argon2id hashing.
- **Sprint 02**: Login, JWT (RS256), token refresh with rotation, logout.
- **Sprint 03**: Session and device management.
- **Sprint 04**: RBAC — roles, permissions, `@PreAuthorize` enforcement.
- **Sprint 05**: Organizations — multi-tenancy, personal orgs, OWNER/ADMIN/MEMBER roles.
- **Sprint 06**: OAuth 2.1 Authorization Server — Authorization Code + Client Credentials flows.
- **Sprint 07**: Passkeys — WebAuthn/FIDO2 passwordless authentication.
- **Sprint 08**: OpenID Connect 1.0 Provider — ID Tokens, UserInfo, Discovery, JWKS.
- **Sprint 09**: OAuth client management REST API — list, get, create OAuth clients (merged with Sprint 06).
- **Sprint 10**: Developer Portal — project management, API keys, secret rotation, usage analytics, rate limits.
- **Sprint 11**: Admin Portal — dashboard, audit logs, announcements, system settings, security incidents.

## Success Criteria

Sprint 11 succeeds when administrators can view platform metrics via the dashboard endpoint, browse audit logs with pagination and filtering, perform CRUD on system announcements, read and update system settings, track and resolve security incidents, audit events are automatically recorded across existing services, all 209 tests pass, and documentation is current.
