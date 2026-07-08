# SecureAuthX API Specification

## Current API Stage

Sprint 10 adds the Developer Portal. Developers can now create projects, manage API keys, rotate OAuth client secrets, view usage analytics, and configure rate limits through a self-service REST API.

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

## OpenID Connect 1.0 Endpoints

OIDC endpoints follow OpenID Connect Core 1.0 and Discovery specifications. The issuer is configured via `SECUREAUTHX_OIDC_ISSUER` (defaults to `http://localhost:8080`).

### `GET /.well-known/openid-configuration`

Returns the OpenID Connect Discovery document describing the OP's capabilities and endpoint URLs.

Public endpoint (no authentication required).

Success response: `200 OK`

```json
{
  "issuer": "http://localhost:8080",
  "authorization_endpoint": "http://localhost:8080/oauth/authorize",
  "token_endpoint": "http://localhost:8080/oauth/token",
  "userinfo_endpoint": "http://localhost:8080/connect/userinfo",
  "jwks_uri": "http://localhost:8080/.well-known/jwks.json",
  "response_types_supported": ["code"],
  "subject_types_supported": ["public"],
  "id_token_signing_alg_values_supported": ["RS256"],
  "scopes_supported": ["openid", "email", "profile"],
  "claims_supported": ["sub", "email", "given_name", "family_name", "iss", "aud", "exp", "iat", "auth_time", "nonce"],
  "claims_parameter_supported": false,
  "request_parameter_supported": false,
  "request_uri_parameter_supported": false
}
```

### `GET /.well-known/jwks.json`

Returns the JSON Web Key Set containing the OP's public RSA key used to sign ID Tokens and Access Tokens.

Public endpoint (no authentication required).

Success response: `200 OK`

```json
{
  "keys": [
    {
      "kty": "RSA",
      "n": "base64url-encoded-modulus",
      "e": "base64url-encoded-exponent",
      "alg": "RS256",
      "use": "sig",
      "kid": "key-id"
    }
  ]
}
```

### `GET /connect/userinfo`

Returns claims about the authenticated end-user. The request must include a valid Bearer JWT access token (either from login or from an OAuth authorization code flow with the `openid` scope).

The endpoint is publicly accessible; the Bearer token is validated inside the controller. Invalid or missing tokens return `401 Unauthorized`.

Success response: `200 OK`

```json
{
  "sub": "user-uuid",
  "email": "user@example.com"
}
```

Error responses:

- `401 Unauthorized` when the access token is missing, expired, or invalid.

### ID Token

When the OAuth authorization request includes `scope=openid`, the token endpoint returns an `id_token` alongside the existing `access_token`, `refresh_token`, and `token_type` fields.

The ID Token is a signed JWT (RS256) with the following standard claims:

| Claim | Description |
|-------|-------------|
| `iss` | Issuer identifier (the OP's issuer URL) |
| `sub` | Subject identifier (user UUID) |
| `aud` | Audience (the OAuth client ID) |
| `exp` | Expiration time (1 hour from issuance) |
| `iat` | Issued at time |
| `auth_time` | Authentication time (when the authorization code was created) |
| `nonce` | Nonce from the authorization request (if provided) |

Example decoded ID Token payload:

```json
{
  "iss": "http://localhost:8080",
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "aud": ["my-client"],
  "exp": 1710000000,
  "iat": 1709996400,
  "auth_time": 1709996400,
  "nonce": "test-nonce-value"
}
```

### OAuth + OIDC Integration

When the scope parameter in the authorize request contains `openid`:

1. The authorization code stores the scope and nonce.
2. During token exchange, the server detects `openid` scope in the stored code.
3. An ID Token is generated and returned in the token response as `id_token`.
4. The scope is echoed back in the `scope` field of the token response.

When the scope does NOT contain `openid`, behavior is identical to Sprint 06 (no ID Token, no scope in response).

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

## Developer Portal Endpoints

All developer portal endpoints require a valid Bearer JWT access token in the `Authorization` header. Users may only manage their own developer projects; admins may access all projects.

### Project Management

#### `POST /api/v1/developer/projects`

Creates a new developer project for the authenticated user. Optionally links an existing OAuth client.

Request:

```json
{
  "name": "My App",
  "description": "A test application",
  "oauthClientId": "550e8400-e29b-41d4-a716-446655440000"
}
```

Validation:
- `name` is required and must be 255 characters or fewer.
- `description` is optional and must be 4000 characters or fewer.
- `oauthClientId` is optional. If provided, the OAuth client is linked and its `owner_user_id` is set.

Success response: `201 Created`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "userId": "660e8400-e29b-41d4-a716-446655440002",
  "name": "My App",
  "description": "A test application",
  "oauthClientId": "550e8400-e29b-41d4-a716-446655440000",
  "enabled": true,
  "createdAt": "2026-07-08T10:00:00Z",
  "updatedAt": "2026-07-08T10:00:00Z"
}
```

Error responses:
- `400 Bad Request` for validation failures.
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the referenced OAuth client does not exist.

#### `GET /api/v1/developer/projects`

Lists all developer projects for the authenticated user.

Success response: `200 OK` (array of `ProjectResponse` objects — same schema as create response)

#### `GET /api/v1/developer/projects/{projectId}`

Returns a specific developer project by UUID. The project must belong to the authenticated user.

Success response: `200 OK` (single `ProjectResponse`)

Error responses:
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the project does not exist or belongs to another user.

#### `PUT /api/v1/developer/projects/{projectId}`

Updates a developer project's name and/or description.

Request:

```json
{
  "name": "Updated App Name",
  "description": "Updated description"
}
```

Validation:
- At least one field (`name` or `description`) should be provided.
- `name` must be 255 characters or fewer.
- `description` must be 4000 characters or fewer.

Success response: `200 OK` (updated `ProjectResponse`)

Error responses:
- `400 Bad Request` for validation failures.
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the project does not exist or belongs to another user.

#### `DELETE /api/v1/developer/projects/{projectId}`

Deletes a developer project and all associated API keys, usage records, and rate limits.

Success response: `204 No Content`

Error responses:
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the project does not exist or belongs to another user.

### API Key Management

#### `POST /api/v1/developer/projects/{projectId}/api-keys`

Creates a new API key for a developer project. The plaintext key is returned only in the create response. The key hash is stored as SHA-256.

Request:

```json
{
  "label": "Production Key",
  "expiresAt": "2027-07-08T10:00:00Z"
}
```

Validation:
- `label` is required and must be 255 characters or fewer.
- `expiresAt` is optional. If not set, the key does not expire.

Success response: `201 Created`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440003",
  "projectId": "550e8400-e29b-41d4-a716-446655440001",
  "keyPrefix": "sk_a1b2c3d4",
  "plainTextKey": "sk_a1b2c3d4e5f6...",
  "label": "Production Key",
  "expiresAt": "2027-07-08T10:00:00Z",
  "createdAt": "2026-07-08T10:00:00Z"
}
```

Error responses:
- `400 Bad Request` for validation failures.
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the project does not exist or belongs to another user.

#### `GET /api/v1/developer/projects/{projectId}/api-keys`

Lists all API keys for a developer project. Key hashes and plaintext keys are never returned.

Success response: `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440003",
    "projectId": "550e8400-e29b-41d4-a716-446655440001",
    "keyPrefix": "sk_a1b2c3d4",
    "label": "Production Key",
    "lastUsedAt": null,
    "expiresAt": "2027-07-08T10:00:00Z",
    "enabled": true,
    "createdAt": "2026-07-08T10:00:00Z"
  }
]
```

Error responses:
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the project does not exist or belongs to another user.

#### `DELETE /api/v1/developer/projects/{projectId}/api-keys/{keyId}`

Revokes an API key by setting it to disabled. The key record is retained for audit purposes.

Success response: `204 No Content`

Error responses:
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the project or key does not exist or belongs to another user.

### Secret Rotation

#### `POST /api/v1/developer/projects/{projectId}/rotate-secret`

Rotates the OAuth client secret for the project's linked OAuth client. The new secret is hashed with Argon2id before storage. The previous secret is immediately invalidated.

The project must have a linked OAuth client (set via `oauthClientId` at creation time).

Success response: `200 OK`

```json
{
  "projectId": "550e8400-e29b-41d4-a716-446655440001",
  "oauthClientId": "550e8400-e29b-41d4-a716-446655440000",
  "newClientSecret": "base64url-new-secret-value",
  "rotatedAt": "2026-07-08T10:00:00Z"
}
```

Error responses:
- `400 Bad Request` when the project has no linked OAuth client.
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the project does not exist or belongs to another user.

### Usage Analytics

#### `GET /api/v1/developer/projects/{projectId}/usage`

Returns daily usage analytics for a developer project within a specified date range.

Query parameters:
| Parameter | Required | Description |
|-----------|----------|-------------|
| `startDate` | Yes | Start date (inclusive) in ISO-8601 format (e.g., `2026-07-01`) |
| `endDate` | Yes | End date (inclusive) in ISO-8601 format (e.g., `2026-07-08`) |

Success response: `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440004",
    "projectId": "550e8400-e29b-41d4-a716-446655440001",
    "date": "2026-07-07",
    "requestCount": 1500,
    "successCount": 1420,
    "failureCount": 80,
    "avgLatencyMs": 45.2,
    "lastRequestAt": "2026-07-07T23:59:00Z",
    "tokenExchanges": 300,
    "userinfoRequests": 150
  }
]
```

Error responses:
- `400 Bad Request` for invalid date parameters.
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the project does not exist or belongs to another user.

### Rate Limit Management

Rate limit configuration is stored but not currently enforced at runtime. This is management-only CRUD.

#### `PUT /api/v1/developer/projects/{projectId}/rate-limits`

Creates or updates rate limit configuration for a developer project.

Request:

```json
{
  "requestsPerMinute": 120,
  "requestsPerHour": 5000
}
```

Validation:
- `requestsPerMinute` must be between 1 and 10000.
- `requestsPerHour` must be between 1 and 1000000.

Success response: `200 OK`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440005",
  "projectId": "550e8400-e29b-41d4-a716-446655440001",
  "requestsPerMinute": 120,
  "requestsPerHour": 5000,
  "enabled": true,
  "createdAt": "2026-07-08T10:00:00Z",
  "updatedAt": "2026-07-08T10:00:00Z"
}
```

Error responses:
- `400 Bad Request` for validation failures.
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the project does not exist or belongs to another user.

#### `GET /api/v1/developer/projects/{projectId}/rate-limits`

Returns the current rate limit configuration for a developer project.

Success response: `200 OK` (single `RateLimitResponse`)

Error responses:
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the project does not exist or belongs to another user, or rate limits are not configured.

#### `DELETE /api/v1/developer/projects/{projectId}/rate-limits`

Removes the rate limit configuration for a developer project.

Success response: `204 No Content`

Error responses:
- `401 Unauthorized` when the access token is missing, expired, or invalid.
- `404 Not Found` when the project does not exist or belongs to another user.

## Authorization

Authentication is performed via RS256 JWT access tokens. OAuth token responses and passkey authentication responses also return RS256 JWT access tokens issued by the same `JwtService`. After JWT validation, the user's roles and permissions are loaded from the database and attached to the security context. This enables both `hasRole()` and `hasAuthority()` checks.

Default role for new users: `ROLE_USER`

Default permissions for `ROLE_USER`: `SESSION_READ`, `SESSION_REVOKE`, `ROLE_READ`

Default permissions for `ROLE_ADMIN`: all permissions

Administrator accounts are not automatically created.

Organization-level roles (`OWNER`, `ADMIN`, `MEMBER`) are stored separately from global RBAC roles and are never merged.

## Versioning

Future product APIs must use `/api/v1/`.
