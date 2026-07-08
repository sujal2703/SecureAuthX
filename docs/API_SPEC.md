# SecureAuthX API Specification

## Current API Stage

Sprint 07 adds WebAuthn/FIDO2 Passkey support for passwordless authentication and registration. Passkey endpoints coexist with existing email/password authentication, OAuth 2.1, and session management. OIDC remains out of scope.

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

## OAuth 2.1 Endpoints

OAuth endpoints use form-urlencoded or JSON request bodies. Token responses follow OAuth 2.1 snake_case conventions (`access_token`, `token_type`, `expires_in`, `refresh_token`).

### Client Management Endpoints

All client management endpoints require `ROLE_ADMIN` and a valid Bearer JWT access token in the `Authorization` header.

#### `POST /api/v1/oauth/clients`

Registers a new OAuth 2.1 client.

Request:

```json
{
  "clientId": "my-client",
  "clientName": "My Client",
  "confidential": true,
  "clientSecret": "my-secret-value",
  "redirectUris": ["https://example.com/callback"]
}
```

Validation:

- `clientId` is required, must be unique.
- `clientName` is required.
- `confidential` defaults to `false`. If `true`, `clientSecret` is required.
- `redirectUris` must contain at least one URI.
- Client secrets are hashed with Argon2id. The raw secret is returned only in the create response.

Success response: `201 Created`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "clientId": "my-client",
  "clientSecret": "my-secret-value",
  "clientName": "My Client",
  "confidential": true,
  "enabled": true,
  "redirectUris": ["https://example.com/callback"],
  "createdAt": "2026-07-08T10:00:00Z"
}
```

The `Location` header is set to the new client's resource URI.

Error responses:

- `400 Bad Request` for validation failures or duplicate `clientId`.
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `403 Forbidden` when the user does not have `ROLE_ADMIN`.

#### `GET /api/v1/oauth/clients`

Lists all registered OAuth 2.1 clients.

Success response: `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "clientId": "my-client",
    "clientName": "My Client",
    "confidential": true,
    "enabled": true,
    "redirectUris": ["https://example.com/callback"],
    "createdAt": "2026-07-08T10:00:00Z",
    "updatedAt": "2026-07-08T10:00:00Z"
  }
]
```

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `403 Forbidden` when the user does not have `ROLE_ADMIN`.

#### `GET /api/v1/oauth/clients/{id}`

Returns a specific OAuth 2.1 client by UUID.

Success response: `200 OK` (same schema as list item)

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `403 Forbidden` when the user does not have `ROLE_ADMIN`.
- `404 Not Found` when the client does not exist.

### Authorization Endpoint

#### `GET /oauth/authorize`

Initiates an OAuth 2.1 Authorization Code Flow with PKCE. The user must be authenticated. On success, the browser is redirected to the registered `redirect_uri` with the authorization code and state.

Query parameters:

| Parameter | Required | Description |
|-----------|----------|-------------|
| `client_id` | Yes | The registered OAuth client ID |
| `redirect_uri` | Yes | Must exactly match a registered redirect URI |
| `response_type` | Yes | Must be `code` |
| `state` | Yes | Opaque value for CSRF protection, echoed back in redirect |
| `code_challenge` | Yes | S256 PKCE challenge derived from the code verifier |
| `code_challenge_method` | Yes | Must be `S256` |
| `scope` | No | Requested scopes (currently not enforced) |

Validation:

- `response_type` must be `code`.
- `code_challenge_method` must be `S256` (plain is not allowed).
- `code_challenge` must be non-empty.
- `client_id` must reference an existing, enabled client.
- `redirect_uri` must exactly match a registered redirect URI for the client.

Success response: `302 Found`

The `Location` header contains the redirect URI with `code` and `state` query parameters:

```
https://example.com/callback?code=abc123...&state=test-state
```

Error responses:

- `400 Bad Request` when client validation fails, redirect URI mismatches, or PKCE challenge is invalid.
- `401 Unauthorized` when the user is not authenticated.
- `403 Forbidden` when the user is not authenticated.

### Token Endpoint

#### `POST /oauth/token`

Exchanges an authorization code for tokens (Authorization Code Flow) or issues tokens directly (Client Credentials Flow).

Request body: `application/x-www-form-urlencoded`

##### Authorization Code Grant

Parameters:

| Parameter | Required | Description |
|-----------|----------|-------------|
| `grant_type` | Yes | Must be `authorization_code` |
| `code` | Yes | The authorization code from the redirect |
| `redirect_uri` | Yes | Must match the URI used in the authorize request |
| `client_id` | Yes | The registered OAuth client ID |
| `client_secret` | No | Required for confidential clients |
| `code_verifier` | Yes | The original PKCE code verifier |

Success response: `200 OK`

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiJ9...",
  "token_type": "Bearer",
  "expires_in": 900,
  "refresh_token": "a1b2c3d4e5f6..."
}
```

##### Client Credentials Grant

Parameters:

| Parameter | Required | Description |
|-----------|----------|-------------|
| `grant_type` | Yes | Must be `client_credentials` |
| `client_id` | Yes | The registered OAuth client ID |
| `client_secret` | Yes | Required for confidential clients |

Success response: `200 OK`

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiJ9...",
  "token_type": "Bearer",
  "expires_in": 900
}
```

Note: `refresh_token` is not returned for the Client Credentials grant.

##### Error Responses

Token errors follow OAuth 2.1 error conventions:

```json
{
  "error": "invalid_grant",
  "error_description": "Authorization code has already been used."
}
```

Error codes:

| Error | HTTP Status | Description |
|-------|-------------|-------------|
| `invalid_request` | 400 | Missing required parameters |
| `invalid_client` | 400 | Client not found, disabled, or secret mismatch |
| `invalid_grant` | 400 | Authorization code invalid, expired, reused, or PKCE verification failed |
| `unsupported_grant_type` | 400 | Grant type is not supported |
| `unauthorized_client` | 400 | Client not authorized for the requested grant type |

Authorization endpoint errors use the standard `ApiErrorResponse` format with error details in `fieldErrors`:

```json
{
  "timestamp": "2026-07-08T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Redirect URI does not match any registered URI.",
  "path": "/oauth/authorize",
  "fieldErrors": {
    "error": "invalid_client"
  }
}
```

## Passkey Endpoints

All passkey endpoints live under `/api/v1/passkeys`.

### `POST /api/v1/passkeys/register/options`

Generates WebAuthn registration options (PublicKeyCredentialCreationOptions) for the authenticated user. Used in step 1 of passkey enrollment.

Requires a valid Bearer JWT access token in the `Authorization` header.

Success response: `200 OK`

```json
{
  "challenge": "base64url-encoded-random-challenge",
  "rp": {
    "name": "SecureAuthX",
    "id": "localhost"
  },
  "user": {
    "id": "user-uuid",
    "name": "user@example.com",
    "displayName": "user@example.com"
  },
  "pubKeyCredParams": [
    {"type": "public-key", "alg": -7},
    {"type": "public-key", "alg": -257}
  ],
  "authenticatorSelection": {
    "residentKey": "required",
    "userVerification": "required",
    "requireResidentKey": true
  },
  "hints": ["security-key", "platform"],
  "attestation": {
    "fmt": "none",
    "alg": -7
  }
}
```

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.

### `POST /api/v1/passkeys/register/verify`

Verifies a WebAuthn registration and stores the passkey for the authenticated user. Used in step 2 of passkey enrollment.

Requires a valid Bearer JWT access token in the `Authorization` header.

Request:

```json
{
  "id": "credential-id",
  "rawId": "base64url-raw-id",
  "type": "public-key",
  "clientDataJSON": "base64url-client-data-json",
  "attestationObject": "base64url-attestation-object",
  "authenticatorData": "base64url-auth-data",
  "publicKey": "base64url-cose-public-key",
  "publicKeyAlgorithm": "-7",
  "transports": "usb,internal,nfc",
  "aaguid": "authenticator-aaguid",
  "deviceName": "YubiKey 5 NFC"
}
```

Success response: `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "verified": true,
  "credentialId": "base64url-credential-id"
}
```

Error responses:

- `400 Bad Request` for invalid challenge, expired challenge, origin mismatch, RP ID mismatch, or malformed attestation.
- `401 Unauthorized` when the access token is missing, expired, or invalid.

### `POST /api/v1/passkeys/authenticate/options`

Generates WebAuthn authentication options (PublicKeyCredentialRequestOptions). This endpoint is public (no authentication required).

Request body is optional. If provided, `userHandle` filters the discoverable credentials for a specific user:

```json
{
  "userHandle": "user-uuid"
}
```

Success response: `200 OK`

```json
{
  "challenge": "base64url-encoded-random-challenge",
  "timeout": 60000,
  "rpId": "localhost",
  "allowCredentials": [
    {
      "type": "public-key",
      "id": "base64url-credential-id",
      "transports": ["usb", "internal", "nfc"]
    }
  ],
  "userVerification": "required"
}
```

When `userHandle` is not provided, `allowCredentials` is an empty array (discoverable credential / resident key flow).

Error responses:

- `400 Bad Request` when the specified user is not found.

### `POST /api/v1/passkeys/authenticate/verify`

Verifies a WebAuthn assertion and issues JWT + refresh tokens on success. This endpoint is public (no authentication required).

Request:

```json
{
  "id": "credential-id",
  "rawId": "base64url-raw-id",
  "type": "public-key",
  "clientDataJSON": "base64url-client-data-json",
  "authenticatorData": "base64url-auth-data",
  "signature": "base64url-signature",
  "userHandle": "user-uuid"
}
```

Success response: `200 OK`

```json
{
  "verified": true,
  "credentialId": "base64url-credential-id",
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "a1b2c3d4e5f6...",
  "expiresIn": 900,
  "tokenType": "Bearer"
}
```

Error responses:

- `400 Bad Request` for invalid challenge, expired challenge, origin mismatch, RP ID hash mismatch, or malformed assertion.
- `401 Unauthorized` when signature verification fails, credential not found, counter is less than stored value, or `userVerification` flag is not set.

### `GET /api/v1/passkeys`

Lists all registered passkeys for the authenticated user.

Requires a valid Bearer JWT access token in the `Authorization` header.

Success response: `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "credentialId": "base64url-credential-id",
    "deviceName": "YubiKey 5 NFC",
    "aaguid": "aaguid-value",
    "credentialType": "public-key",
    "backedUp": false,
    "createdAt": "2026-07-08T10:00:00Z",
    "updatedAt": "2026-07-08T10:00:00Z"
  }
]
```

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.

### `DELETE /api/v1/passkeys/{id}`

Deletes a specific passkey by UUID. The passkey must belong to the authenticated user.

Requires a valid Bearer JWT access token in the `Authorization` header.

Success response: `204 No Content`

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the passkey does not exist or belongs to another user.

## Authorization

Authentication is performed via RS256 JWT access tokens. OAuth token responses and passkey authentication responses also return RS256 JWT access tokens issued by the same `JwtService`. After JWT validation, the user's roles and permissions are loaded from the database and attached to the security context. This enables both `hasRole()` and `hasAuthority()` checks.

Default role for new users: `ROLE_USER`

Default permissions for `ROLE_USER`: `SESSION_READ`, `SESSION_REVOKE`, `ROLE_READ`

Default permissions for `ROLE_ADMIN`: all permissions

Administrator accounts are not automatically created.

Organization-level roles (`OWNER`, `ADMIN`, `MEMBER`) are stored separately from global RBAC roles and are never merged.

## Versioning

Future product APIs must use `/api/v1/`.
