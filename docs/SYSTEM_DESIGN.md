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
- `oauth`: OAuth 2.1 authorization server. Contains controller, service, repository, entities, DTOs, PKCE service, and OAuth-specific exceptions.
- `passkey`: WebAuthn/FIDO2 passkey support. Contains controller, services (registration, authentication, passkey CRUD, challenge management, COSE key parsing), repository, entities, DTOs, and passkey-specific exceptions.
- `oidc`: OpenID Connect 1.0 Provider. Contains controller (discovery, JWKS, UserInfo), service (ID Token generation, JWK serialization), and DTOs.
- `developer`: Developer Portal. Contains controller, services (project management, API key management, secret rotation, usage analytics, rate limit management), repository, entities, DTOs, and developer-specific exceptions.
- `admin`: Admin Portal. Contains controller, services (audit, dashboard, announcements, system settings, incidents), repository, entities, DTOs, and admin-specific exceptions.
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

Sprint 06 OAuth 2.1 endpoints are publicly accessible (no authentication on the HTTP security layer; authorization endpoint uses `@PreAuthorize`):

- `GET /oauth/authorize` — public at HTTP level, `@PreAuthorize("isAuthenticated()")` at method level
- `POST /oauth/token` — fully public

Sprint 06 client management endpoints require `ROLE_ADMIN`:

- `POST /api/v1/oauth/clients`
- `GET /api/v1/oauth/clients`
- `GET /api/v1/oauth/clients/{id}`

Sprint 07 passkey endpoints:

- `POST /api/v1/passkeys/authenticate/options` — public
- `POST /api/v1/passkeys/authenticate/verify` — public
- `POST /api/v1/passkeys/register/options` — requires authentication (`@PreAuthorize("isAuthenticated()")`)
- `POST /api/v1/passkeys/register/verify` — requires authentication (`@PreAuthorize("isAuthenticated()")`)
- `GET /api/v1/passkeys` — requires authentication (`@PreAuthorize("isAuthenticated()")`)
- `DELETE /api/v1/passkeys/{id}` — requires authentication (`@PreAuthorize("isAuthenticated()")`)

Sprint 08 OIDC endpoints:

- `GET /.well-known/openid-configuration` — public
- `GET /.well-known/jwks.json` — public
- `GET /connect/userinfo` — public at HTTP layer; Bearer token validated in controller

Sprint 11 Admin Portal endpoints require `ROLE_ADMIN` and a valid Bearer JWT access token:

- `GET /api/v1/admin/dashboard`
- `GET /api/v1/admin/audit`
- `GET /api/v1/admin/audit/{id}`
- `POST /api/v1/admin/announcements`
- `GET /api/v1/admin/announcements`
- `GET /api/v1/admin/announcements/{id}`
- `PUT /api/v1/admin/announcements/{id}`
- `DELETE /api/v1/admin/announcements/{id}`
- `GET /api/v1/admin/settings`
- `GET /api/v1/admin/settings/{key}`
- `PUT /api/v1/admin/settings/{key}`
- `GET /api/v1/admin/incidents`
- `GET /api/v1/admin/incidents/{id}`
- `PUT /api/v1/admin/incidents/{id}/resolve`

Sprint 10 Developer Portal endpoints require a valid Bearer JWT access token:

- `POST /api/v1/developer/projects`
- `GET /api/v1/developer/projects`
- `GET /api/v1/developer/projects/{projectId}`
- `PUT /api/v1/developer/projects/{projectId}`
- `DELETE /api/v1/developer/projects/{projectId}`
- `POST /api/v1/developer/projects/{projectId}/api-keys`
- `GET /api/v1/developer/projects/{projectId}/api-keys`
- `DELETE /api/v1/developer/projects/{projectId}/api-keys/{keyId}`
- `POST /api/v1/developer/projects/{projectId}/rotate-secret`
- `GET /api/v1/developer/projects/{projectId}/usage`
- `PUT /api/v1/developer/projects/{projectId}/rate-limits`
- `GET /api/v1/developer/projects/{projectId}/rate-limits`
- `DELETE /api/v1/developer/projects/{projectId}/rate-limits`

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

### OAuth 2.1 Authorization Code Flow

1. Client redirects the resource owner (authenticated user) to `GET /oauth/authorize` with `client_id`, `redirect_uri`, `response_type=code`, `state`, `code_challenge` (S256), and `code_challenge_method=S256`.
2. Server validates: client exists and is enabled, redirect URI matches a registered URI exactly, PKCE challenge method is S256, and the user is authenticated.
3. Server generates a random authorization code (32 bytes, Base64 URL-encoded), stores it with the PKCE challenge, user, client, and redirect URI. Code expires in 10 minutes.
4. Server redirects the browser to `redirect_uri?code=...&state=...` (302 Found).
5. Client sends `POST /oauth/token` with `grant_type=authorization_code`, `code`, `redirect_uri`, `client_id`, and `code_verifier`.
6. Server validates: code exists, not consumed, not expired, belongs to the same client and redirect URI. PKCE verification: SHA-256 hash of `code_verifier` must match the stored `code_challenge`.
7. Server marks the code as consumed (single-use). Creates a session and refresh token (SHA-256 hashed) for the user. Issues the same RS256 JWT access token used by the login flow.
8. Client receives `{"access_token", "token_type": "Bearer", "expires_in", "refresh_token"}`.

### OAuth 2.1 Client Credentials Flow

1. Client sends `POST /oauth/token` with `grant_type=client_credentials`, `client_id`, and `client_secret`.
2. Server validates: client exists, is enabled, is confidential (has a client secret), and the secret matches the Argon2id hash.
3. Server issues an RS256 JWT access token with a random UUID subject. No refresh token, no session is created.
4. Client receives `{"access_token", "token_type": "Bearer", "expires_in"}` (no `refresh_token`).

### OpenID Connect Flow

1. **Authorization Request**: RP redirects the End-User to `GET /oauth/authorize` with `scope=openid` (plus optional `nonce`), along with the standard OAuth parameters (`client_id`, `redirect_uri`, `response_type=code`, `state`, `code_challenge`, `code_challenge_method=S256`).

2. **Authorization Code Issuance**: Server validates the request (same as Sprint 06), includes `nonce` and `scope` in the stored authorization code record.

3. **Token Exchange**: RP sends `POST /oauth/token` with the authorization code, PKCE verifier, and client credentials. Server validates as before.

4. **ID Token Generation**: When the stored authorization code has `scope` containing `openid`, the server generates an RS256-signed ID Token with claims: `iss` (issuer URL), `sub` (user UUID), `aud` (client ID), `exp`, `iat`, `auth_time` (from authorization code creation), and `nonce` (if provided in the authorization request). The ID Token is returned in the token response as `id_token`.

5. **UserInfo**: RP calls `GET /connect/userinfo` with the access token as a Bearer token. Server validates the token, looks up the user, and returns `sub` (user UUID) and `email`.

6. **Key Discovery**: RP fetches `GET /.well-known/openid-configuration` for OP metadata, then `GET /.well-known/jwks.json` for the public RSA key to verify the ID Token signature.

### Passkey Authentication Flow

1. **Registration — Generate Options**: Authenticated user calls `POST /api/v1/passkeys/register/options`. Server generates a random challenge (Base64 URL-encoded, 5-minute expiry), stores it as a `webauthn_challenges` record with purpose `REGISTER`, and returns `PublicKeyCredentialCreationOptions` (RP info, user info, supported algorithms including ES256 and RS256, resident key + user verification required, hints for security-key and platform authenticators).

2. **Registration — Verify**: Authenticated user calls `POST /api/v1/passkeys/register/verify` with the authenticator's response (credential ID, client data JSON, attestation object, COSE public key, transports, AAGUID, device name). Server validates: challenge exists, not used, not expired, purpose is `REGISTER`, origin matches configured RP origin (`SECUREAUTHX_PASSKEY_RP_ORIGIN`), RP ID hash in authenticator data matches configured RP ID (`SECUREAUTHX_PASSKEY_RP_ID`). The COSE public key is parsed via `CoseKeyParser` (supports EC2 P-256/P-384/P-521 and RSA). The passkey is saved with initial counter 0.

3. **Authentication — Generate Options**: Client (authenticated or not) calls `POST /api/v1/passkeys/authenticate/options` with an optional `userHandle`. If provided, server returns only credentials matching that user. Server generates a random challenge (same 5-minute pattern), stores it with purpose `AUTHENTICATE`, and returns `PublicKeyCredentialRequestOptions`.

4. **Authentication — Verify**: Client calls `POST /api/v1/passkeys/authenticate/verify` with credential ID, authenticator data, client data JSON, signature, and user handle. Server validates: challenge exists, not used, not expired, purpose is `AUTHENTICATE`, origin matches, RP ID hash matches, user verification flag is set, credential exists and belongs to the user, counter is greater than stored value (monotonic increase enforced). The COSE public key from the stored passkey is parsed and used to verify the assertion signature. On success, the counter is updated, a JWT access token + refresh token pair is issued, and a session is created (same `JwtService` and `SessionService` as the login flow). On failure, returns `401 Unauthorized` without distinguishing the specific failure reason.

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
