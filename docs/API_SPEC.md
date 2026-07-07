# SecureAuthX API Specification

## Current API Stage

Sprint 02 adds user login, token refresh, and logout. OAuth, OIDC, passkeys, RBAC, and session management remain out of scope.

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
- Refresh tokens expire after 7 days (configurable via `SECUREAUTHX_JWT_REFRESH_TOKEN_EXPIRATION_DAYS`).
- Refresh tokens are stored as SHA-256 hashes. Plaintext refresh tokens are never stored.
- Token rotation: each refresh invalidates the previous refresh token.

## Versioning

Future product APIs must use `/api/v1/`.
