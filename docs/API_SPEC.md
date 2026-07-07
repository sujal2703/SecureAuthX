# SecureAuthX API Specification

## Current API Stage

Sprint 05 adds organizations and multi-tenancy foundation. OAuth, OIDC, and passkeys remain out of scope.

## Public Foundation Endpoints

### `GET /actuator/health`

Returns Spring Boot actuator health status.

Expected success response:

```json
{
  "status": "UP"
}
```

### `GET /v3/api-docs`

Returns the OpenAPI document.

### `GET /swagger-ui`

Serves Swagger UI.

## Security

The registration endpoint is public. Login, refresh, and logout endpoints are public.
Access tokens are signed with RS256. Refresh tokens are stored as SHA-256 hashes.
All other non-foundation routes are denied by default until product authorization is implemented.

## Authentication Endpoints

### `POST /api/v1/auth/register`

Registers a user with email and password credentials.

Request:

```json
{
  "email": "developer@example.com",
  "password": "S3cureExample!2026"
}
```

Validation:

- `email` is required, must be valid, and must be 320 characters or fewer.
- `password` is required, must be 12 to 128 characters, and must include uppercase, lowercase, number, and special characters.

Success response: `201 Created`

```json
{
  "id": "49ba9128-32d8-4cf1-b136-3edb77a969f6",
  "email": "developer@example.com",
  "createdAt": "2026-07-06T17:00:00Z"
}
```

Error responses:

- `400 Bad Request` for validation failures.
- `409 Conflict` when the email is already registered.

Passwords are never returned by the API.

### `POST /api/v1/auth/login`

Authenticates a user with email and password. Returns an access token and refresh token.

Request:

```json
{
  "email": "developer@example.com",
  "password": "S3cureExample!2026"
}
```

Validation:

- `email` is required, must be valid, and must be 320 characters or fewer.
- `password` is required.

Success response: `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "a1b2c3d4e5f6...",
  "expiresIn": 900,
  "tokenType": "Bearer"
}
```

Error responses:

- `400 Bad Request` for validation failures.
- `401 Unauthorized` when email or password is incorrect. The error message does not distinguish between invalid email and invalid password.

### `POST /api/v1/auth/refresh`

Exchanges a valid refresh token for a new access token and refresh token. The previous refresh token is revoked (token rotation).

Request:

```json
{
  "refreshToken": "a1b2c3d4e5f6..."
}
```

Success response: `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "new-refresh-token-value",
  "expiresIn": 900,
  "tokenType": "Bearer"
}
```

Error responses:

- `400 Bad Request` for validation failures.
- `401 Unauthorized` when the refresh token is invalid, expired, or revoked.

### `POST /api/v1/auth/logout`

Revokes the provided refresh token. Does not invalidate other sessions for the same user.

Request:

```json
{
  "refreshToken": "a1b2c3d4e5f6..."
}
```

Success response: `204 No Content`

Error responses:

- `400 Bad Request` for validation failures.

## Token Details

- Access tokens are JWTs signed with RS256.
- Access tokens expire after 15 minutes (configurable via `SECUREAUTHX_JWT_ACCESS_TOKEN_EXPIRATION_MINUTES`).
- Access tokens contain a `sessionId` claim for session management.
- Refresh tokens expire after 7 days (configurable via `SECUREAUTHX_JWT_REFRESH_TOKEN_EXPIRATION_DAYS`).
- Refresh tokens are stored as SHA-256 hashes. Plaintext refresh tokens are never stored.
- Token rotation: each refresh invalidates the previous refresh token.

## Session Management Endpoints

All session endpoints require a valid Bearer JWT access token in the `Authorization` header.

### `GET /api/v1/sessions`

Lists all active (non-revoked, non-expired) sessions for the authenticated user. The current session (the one used to make this request) is marked with `isCurrent: true`.

Success response: `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "createdAt": "2026-07-07T10:00:00Z",
    "lastActivityAt": "2026-07-07T10:30:00Z",
    "expiresAt": "2026-07-14T10:00:00Z",
    "isRevoked": false,
    "isExpired": false,
    "ipAddress": "192.168.1.100",
    "deviceName": "Windows PC",
    "operatingSystem": "Windows",
    "browser": "Chrome",
    "isCurrent": true
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "createdAt": "2026-07-06T15:00:00Z",
    "lastActivityAt": "2026-07-06T15:30:00Z",
    "expiresAt": "2026-07-13T15:00:00Z",
    "isRevoked": false,
    "isExpired": false,
    "ipAddress": "10.0.0.50",
    "deviceName": "iPhone",
    "operatingSystem": "iOS",
    "browser": "Safari",
    "isCurrent": false
  }
]
```

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.

### `GET /api/v1/sessions/current`

Returns details about the current session (the one used to make the request).

Success response: `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2026-07-07T10:00:00Z",
  "lastActivityAt": "2026-07-07T10:30:00Z",
  "expiresAt": "2026-07-14T10:00:00Z",
  "isRevoked": false,
  "isExpired": false,
  "ipAddress": "192.168.1.100",
  "deviceName": "Windows PC",
  "operatingSystem": "Windows",
  "browser": "Chrome",
  "isCurrent": true
}
```

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when no active session exists.

### `DELETE /api/v1/sessions/{sessionId}`

Revokes a specific session by ID. The associated refresh token is also revoked.

Success response: `204 No Content`

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the session does not exist or belongs to another user.

### `DELETE /api/v1/sessions/current`

Revokes the current session. Equivalent to logout from the current device.

Success response: `204 No Content`

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.

### `DELETE /api/v1/sessions/all`

Revokes all active sessions for the authenticated user. All associated refresh tokens are also revoked.

Success response: `204 No Content`

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.

## RBAC Endpoints

All RBAC endpoints require a valid Bearer JWT access token in the `Authorization` header.

### `GET /api/v1/roles`

Lists all available roles. Only exposes role id, name, and description. Internal mappings are not exposed.

Success response: `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "USER",
    "description": "Standard user with basic permissions"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "ADMIN",
    "description": "Administrator with elevated permissions"
  }
]
```

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.

### `GET /api/v1/permissions`

Lists all available permissions. Only exposes permission id, name, and description.

Success response: `200 OK`

```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "name": "USER_READ",
    "description": "View user details"
  },
  {
    "id": "880e8400-e29b-41d4-a716-446655440003",
    "name": "USER_WRITE",
    "description": "Create or update users"
  }
]
```

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.

## Organization Endpoints

All organization endpoints require a valid Bearer JWT access token in the `Authorization` header.

### `GET /api/v1/organizations`

Lists all organizations the authenticated user belongs to. Includes the user's role within each organization.

Success response: `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "My Org",
    "slug": "my-org",
    "isPersonal": true,
    "role": "OWNER",
    "createdAt": "2026-07-07T10:00:00Z",
    "updatedAt": "2026-07-07T10:00:00Z"
  }
]
```

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.

### `GET /api/v1/organizations/current`

Returns the authenticated user's personal organization. Every user has exactly one personal organization created at registration time.

Success response: `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "My Org",
  "slug": "my-org",
  "isPersonal": true,
  "role": "OWNER",
  "createdAt": "2026-07-07T10:00:00Z",
  "updatedAt": "2026-07-07T10:00:00Z"
}
```

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the user has no personal organization.

### `POST /api/v1/organizations`

Creates a new non-personal organization. The authenticated user becomes the `OWNER` of the new organization.

Request:

```json
{
  "name": "Acme Corp"
}
```

Validation:

- `name` is required and must be 255 characters or fewer.

Success response: `201 Created`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Acme Corp",
  "slug": "acme-corp",
  "isPersonal": false,
  "role": "OWNER",
  "createdAt": "2026-07-07T10:00:00Z",
  "updatedAt": "2026-07-07T10:00:00Z"
}
```

The `Location` header is set to the new organization's resource URI.

Error responses:

- `400 Bad Request` for validation failures.
- `401 Unauthorized` when the access token is missing, expired, or invalid.

### `PATCH /api/v1/organizations/{organizationId}`

Updates organization metadata. Only `OWNER` or `ADMIN` members can update an organization.

Request:

```json
{
  "name": "Updated Corp"
}
```

Validation:

- At least one field must be provided.

Success response: `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Updated Corp",
  "slug": "updated-corp",
  "isPersonal": false,
  "role": "OWNER",
  "createdAt": "2026-07-07T10:00:00Z",
  "updatedAt": "2026-07-07T10:00:00Z"
}
```

Error responses:

- `400 Bad Request` for validation failures.
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `403 Forbidden` when the user is not an `OWNER` or `ADMIN` of the organization.
- `404 Not Found` when the organization does not exist or the user is not a member.

## Authorization

Authentication is performed via RS256 JWT access tokens. After JWT validation, the user's roles and permissions are loaded from the database and attached to the security context. This enables both `hasRole()` and `hasAuthority()` checks.

Default role for new users: `ROLE_USER`

Default permissions for `ROLE_USER`: `SESSION_READ`, `SESSION_REVOKE`, `ROLE_READ`

Default permissions for `ROLE_ADMIN`: all permissions

Administrator accounts are not automatically created.

Organization-level roles (`OWNER`, `ADMIN`, `MEMBER`) are stored separately from global RBAC roles and are never merged.

## Versioning

Future product APIs must use `/api/v1/`.
