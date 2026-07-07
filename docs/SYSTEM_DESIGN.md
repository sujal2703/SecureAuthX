# SecureAuthX System Design

## Architecture

SecureAuthX uses a modular monolith architecture. The executable application is the Spring Boot backend in `backend/server`.

Feature modules live under `com.secureauthx.server` using feature-first package boundaries and the layered pattern defined in the engineering handbook.

## Current Backend Modules

- `auth`: registration, login, token refresh, and logout. Contains controller, service, repository, entities, DTOs, mapper, validation, JWT service, and auth-specific exceptions.
- `sessions`: session and device management. Contains controller, service, repository, entity, DTOs, UserAgentParser, and session-specific exceptions.
- `authorization`: role-based access control. Contains controller, service, repository, entities, DTOs, and authority loading.
- `organization`: organizations and multi-tenancy foundation. Contains controller, service, repository, entities, DTOs, and org-specific exceptions.
- `common`: shared API error response and global exception handling.
- `config`: security, JWT authentication filter, and OpenAPI configuration.

## Runtime Components

- Backend: Spring Boot 3.x application.
- Database: PostgreSQL 17.
- Cache: Redis 7 (not yet used by auth; reserved for future session state).
- Migration tool: Flyway.
- API documentation: Springdoc OpenAPI.
- Health checks: Spring Boot Actuator.

## Local Development Topology

`docker-compose.yml` starts:

- `postgres`
- `redis`
- `backend`

The backend depends on healthy PostgreSQL and Redis containers before startup.

## Security Defaults

The foundation exposes only operational and documentation endpoints publicly:

- `/actuator/health`
- `/actuator/health/**`
- `/actuator/info`
- `/v3/api-docs`
- `/v3/api-docs/**`
- `/swagger-ui`
- `/swagger-ui/**`

Sprint 02 exposes the following authentication endpoints publicly:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

Sprint 03 session endpoints require a valid Bearer JWT access token:

- `GET /api/v1/sessions`
- `GET /api/v1/sessions/current`
- `DELETE /api/v1/sessions/{sessionId}`
- `DELETE /api/v1/sessions/current`
- `DELETE /api/v1/sessions/all`

Sprint 04 RBAC endpoints require a valid Bearer JWT access token:

- `GET /api/v1/roles`
- `GET /api/v1/permissions`

Sprint 05 organization endpoints require a valid Bearer JWT access token:

- `GET /api/v1/organizations`
- `GET /api/v1/organizations/current`
- `POST /api/v1/organizations`
- `PATCH /api/v1/organizations/{organizationId}`

All other routes are denied by default until authorization flows are implemented in later sprints.

## Authorization Model

Access control is role-based with permission-level granularity. The model uses four tables: `roles`, `permissions`, `user_roles`, and `role_permissions`.

On every authenticated request, the `JwtAuthenticationFilter` loads the user's roles and permissions from the database and attaches them as `GrantedAuthority` objects to the Spring Security context. Roles are prefixed with `ROLE_` (e.g., `ROLE_USER`), enabling `hasRole('USER')` checks. Permissions are used as-is (e.g., `SESSION_READ`), enabling `hasAuthority('SESSION_READ')` checks.

Method-level security is enabled via `@EnableMethodSecurity`. Controllers use `@PreAuthorize` annotations for declarative access control.

Every newly registered user automatically receives `ROLE_USER`. No administrator is automatically created.

## Organization Model

Organizations provide a multi-tenancy foundation. Each user has exactly one personal organization (created automatically at registration) and can create or join additional organizations.

Organization-level roles (`OWNER`, `ADMIN`, `MEMBER`) govern what a user can do within an organization. These roles are stored in the `organization_members` table, separate from the global RBAC `roles` table, and are never merged.

- `OWNER`: full control over the organization (rename, manage members, delete)
- `ADMIN`: can update organization metadata
- `MEMBER`: read-only access to the organization

On every authenticated organization request, the `OrganizationService` loads the user's organization role from the database and enforces access rules. Ownership or admin-level authorization (`OWNER` or `ADMIN`) is required for mutation operations.

## Authentication Flow

1. **Registration**: User submits email and password via `POST /api/v1/auth/register`. Password is hashed with Argon2id. User record is created. A personal organization is automatically created with the user as `OWNER`. No session or token is issued.

2. **Login**: User submits email and password via `POST /api/v1/auth/login`. Password is verified against the Argon2id hash. On success, an RS256-signed JWT access token and a refresh token are returned. The refresh token is stored as a SHA-256 hash. A session record is created with device information parsed from the User-Agent header.

3. **Token Refresh**: Client submits the refresh token via `POST /api/v1/auth/refresh`. The token hash is verified. If valid, the old refresh token is revoked and new access and refresh tokens are issued (token rotation). A new session is created for the new device context.

4. **Logout**: Client submits the refresh token via `POST /api/v1/auth/logout`. The refresh token is revoked. Other sessions for the same user remain active.

## JWT Configuration

- Algorithm: RS256 (asymmetric RSA with SHA-256)
- Key size: 2048 bits
- Access token expiry: 15 minutes (configurable)
- Refresh token expiry: 7 days (configurable)
- Access token claims: `sub` (user id), `email`, `sessionId`
- Key pair: generated on startup, or configured via environment variables for production

## Session and Device Management

Session records are created on login and refresh. Each session captures:

- The device that initiated the session (browser, OS, device name via User-Agent parsing)
- The IP address at login time
- A link to the associated refresh token for simultaneous revocation

The JWT access token carries a `sessionId` claim, enabling the API to identify and return the current session without additional queries. Sessions expire after 7 days (matching the refresh token lifetime).

## Configuration

Runtime configuration is supplied through environment variables. Local examples are documented in `.env.example`.
