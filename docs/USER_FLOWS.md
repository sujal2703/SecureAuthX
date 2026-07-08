# SecureAuthX User Flows

## Current Stage

Sprint 06 includes OAuth 2.1 Authorization Server with Authorization Code Flow (PKCE S256 mandatory) and Client Credentials Flow.

## Developer Foundation Flow

1. Copy `.env.example` to `.env`.
2. Replace local secret values.
3. Run `docker compose up --build`.
4. Verify backend health at `/actuator/health`.
5. Verify OpenAPI at `/v3/api-docs` or `/swagger-ui`.

## Registration Flow

1. Client submits `POST /api/v1/auth/register` with email and password.
2. Backend validates email format and password strength.
3. Backend normalizes email to lowercase.
4. Backend rejects duplicate emails with `409 Conflict`.
5. Backend hashes the password with Argon2id.
6. Backend stores the user record.
7. Backend assigns `ROLE_USER` to the new user.
8. Backend creates a personal organization with the user as `OWNER`.
9. Backend returns `201 Created` with user id, normalized email, and creation timestamp.

## Role and Permission Listing Flow

1. Client authenticates via login to obtain a JWT access token.
2. Client uses the Bearer JWT to call `GET /api/v1/roles` or `GET /api/v1/permissions`.
3. Backend validates the JWT, loads user authorities, and returns the requested data.
4. Responses contain only safe information (id, name, description) — no internal mappings.

## Organization Flows

### List Organizations

1. Client authenticates via login to obtain a JWT access token.
2. Client uses the Bearer JWT to call `GET /api/v1/organizations`.
3. Backend validates the JWT and returns all organizations the user belongs to, including the user's role within each.

### Get Personal Organization

1. Client authenticates via login to obtain a JWT access token.
2. Client uses the Bearer JWT to call `GET /api/v1/organizations/current`.
3. Backend validates the JWT and returns the user's personal organization.

### Create Organization

1. Client authenticates via login to obtain a JWT access token.
2. Client uses the Bearer JWT to call `POST /api/v1/organizations` with a name.
3. Backend creates the organization and adds the user as `OWNER`.
4. A URL-safe slug is auto-generated from the name.

### Update Organization

1. Client authenticates via login to obtain a JWT access token.
2. Client uses the Bearer JWT to call `PATCH /api/v1/organizations/{organizationId}` with updated fields.
3. Backend verifies the user is an `OWNER` or `ADMIN` of the organization.
4. Backend updates the organization and returns the updated record.

## OAuth 2.1 Flows

### Admin Client Management Flow

1. Admin authenticates via login to obtain a JWT access token (must have `ROLE_ADMIN`).
2. Admin uses the Bearer JWT to call `POST /api/v1/oauth/clients` with a client ID, name, confidential flag, optional secret, and redirect URIs.
3. Backend validates the request, hashes the client secret with Argon2id (if provided), stores the client and redirect URIs, and returns the client details including the raw secret (only time the secret is visible).
4. Admin can list all clients via `GET /api/v1/oauth/clients` or get a specific client via `GET /api/v1/oauth/clients/{id}`.

### Authorization Code Flow with PKCE

1. **Prerequisite**: An admin registers an OAuth client (public or confidential). The client generates a PKCE code verifier (43–128 characters) and computes the S256 code challenge.
2. **Authorization Request**: Client redirects the user's browser to `GET /oauth/authorize` with `client_id`, `redirect_uri`, `response_type=code`, `state`, `code_challenge`, and `code_challenge_method=S256`.
3. **User Authentication**: The user must be authenticated. If not, the server returns 403 Forbidden.
4. **Validation**: Server validates the client exists, is enabled, the redirect URI is registered, the response type is `code`, and the PKCE method is `S256`.
5. **Code Issuance**: Server generates a random 32-byte authorization code (Base64 URL-encoded, 10-minute expiry), stores it with the PKCE challenge and user context, and redirects the browser to the registered redirect URI with `code` and `state` appended.
6. **Token Exchange**: Client sends `POST /oauth/token` with `grant_type=authorization_code`, `code`, `redirect_uri`, `client_id`, and `code_verifier`. If the client is confidential, `client_secret` is also required.
7. **PKCE Verification**: Server computes SHA-256 of the `code_verifier` and compares it to the stored `code_challenge`.
8. **Code Consumption**: Server marks the code as consumed (single-use) and issues an RS256 JWT access token and a refresh token. A new session is created.
9. **API Access**: Client uses the `access_token` as a Bearer token for API calls. Client can use the `refresh_token` for token rotation via `POST /api/v1/auth/refresh`.

### Client Credentials Flow

1. **Prerequisite**: An admin registers a **confidential** OAuth client (with a client secret).
2. **Token Request**: Client sends `POST /oauth/token` with `grant_type=client_credentials`, `client_id`, and `client_secret`.
3. **Validation**: Server validates the client exists, is enabled, is confidential, and the secret matches the Argon2id hash.
4. **Token Issuance**: Server issues an RS256 JWT access token with a random UUID subject (no user context). No refresh token or session is created.
5. **API Access**: Client uses the `access_token` as a Bearer token for API calls.

## Future Flows

Password reset, email verification, OpenID Connect, passkeys, role assignment endpoints, invitation flows, member management, and developer portal are future sprint work.
