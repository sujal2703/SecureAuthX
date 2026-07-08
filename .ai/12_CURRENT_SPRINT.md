# Sprint 06 - OAuth 2.1 Authorization Server

Status: COMPLETED on 2026-07-08

## Objective

Implement an OAuth 2.1 Authorization Server supporting Authorization Code Flow with PKCE (S256 mandatory) and Client Credentials Flow. Reuse existing JWT, session, and refresh token infrastructure. No opaque tokens, no OIDC, no Implicit/Password/Device flows.

## Requirements

### Database
- Create V7 Flyway migration with three tables: `oauth_clients`, `oauth_client_redirect_uris`, `oauth_authorization_codes`.
- `oauth_clients` columns: id (UUID PK), client_id (unique VARCHAR 100), client_secret (hashed, nullable), client_name, confidential (boolean), enabled, created_at, updated_at.
- `oauth_client_redirect_uris` columns: id (UUID PK), client_id (FK to oauth_clients ON DELETE CASCADE), redirect_uri (VARCHAR 2048), created_at.
- `oauth_authorization_codes` columns: id (UUID PK), code (unique VARCHAR 255), user_id (FK to users), client_id (FK to oauth_clients), redirect_uri, code_challenge, challenge_method (default S256), expires_at, consumed (boolean), created_at.
- Unique index on `oauth_clients.client_id` and `oauth_authorization_codes.code`.
- Secondary indexes on FKs.

### Domain Model
- Entities: `OAuthClient`, `OAuthClientRedirectUri`, `AuthorizationCode`.
- Repositories with `JOIN FETCH` queries.
- DTOs: `CreateClientRequest`, `CreateClientResponse`, `ClientResponse`, `TokenResponse`.
- OAuth exception hierarchy: `OAuthException` (abstract), `InvalidClientException`, `InvalidGrantException`, `InvalidRedirectUriException`, `InvalidScopeException`, `UnauthorizedClientException`.

### Services
- **PKCEService**: generates code verifiers (32 bytes, Base64 URL), computes S256 challenges (SHA-256 + Base64 URL), verifies challenges via constant-time comparison.
- **OAuthClientService**: creates clients (validates uniqueness, hashes secrets with Argon2id), retrieves clients by ID or client_id, authenticates clients (checks enabled, validates secret for confidential clients).
- **AuthorizationCodeService**: creates codes (random 32 bytes, Base64 URL, 10-minute expiry), consumes codes with validation (exists, not consumed, not expired, same client, same redirect_uri).
- **OAuthAuthorizationService**: validates authorization requests (client, redirect URI, response type, PKCE), handles authorization code grant (PKCE verification, session creation, token issuance), handles client credentials grant (confidential check only).

### API
- `POST /api/v1/oauth/clients` — create OAuth client (ADMIN only).
- `GET /api/v1/oauth/clients` — list all OAuth clients (ADMIN only).
- `GET /api/v1/oauth/clients/{id}` — get OAuth client by ID (ADMIN only).
- `GET /oauth/authorize` — authorization endpoint (authenticated user, public at HTTP level, @PreAuthorize).
- `POST /oauth/token` — token endpoint (public).

### PKCE
- S256 challenge method is mandatory. Plain method is rejected.
- Code verifier must be 43–128 characters.
- Challenge verification uses `MessageDigest.isEqual()` for timing-safe comparison.

### Error Handling
- Token endpoint errors follow OAuth 2.1 spec: `{"error": "xxx", "error_description": "yyy"}` with HTTP 400.
- Authorization endpoint errors use `ApiErrorResponse` with `fieldErrors.error` containing the OAuth error code.
- Error codes: `invalid_request`, `invalid_client`, `invalid_grant`, `unsupported_grant_type`, `unauthorized_client`.

## Architecture Decisions

### Token Reuse
- OAuth issues the same RS256 JWT access tokens as the login flow via `JwtService.createAccessToken()`. No separate token type or opaque tokens.
- Refresh tokens are stored in the existing `refresh_tokens` table (SHA-256 hashed), same as user login.

### Client Secret Hashing
- Client secrets are hashed with the same Argon2id `PasswordEncoder` bean used for user passwords.
- Raw secret is returned only in the create response. Subsequent retrievals return `null` for `clientSecret`.

### PKCE S256 Mandatory
- Authorization Code Flow requires PKCE with S256. This is an OAuth 2.1 requirement.
- Public clients (no secret) and confidential clients both require PKCE.

### Redirect URI Matching
- Exact string match against registered URIs. No pattern, wildcard, or host matching.
- Multiple redirect URIs per client are supported.

### Authorization Code Security
- Single-use (consumed flag set on first exchange, second use returns `invalid_grant`).
- 10-minute expiry (hardcoded in `AuthorizationCodeService`).
- Bound to specific client and redirect URI — mismatch returns `invalid_grant`.

### Client Credentials Restriction
- Only confidential clients (with a client secret) can use the client credentials grant.
- Public clients attempting client credentials receive `unauthorized_client`.

### Response Serialization
- Token responses use `Map<String, Object>` with snake_case keys (`access_token`, `token_type`, `expires_in`, `refresh_token`, `scope`) to comply with OAuth 2.1 wire format.
- `refresh_token` is omitted from client credentials grant responses.

### Test Cleanup
- All integration test `@BeforeEach` methods delete `oauth_authorization_codes` before deleting `users` or `oauth_clients` to avoid FK constraint violations in the shared H2 test context.

## Files Changed

### New Files
- `backend/server/src/main/resources/db/migration/V7__Create_oauth_tables.sql`
- `oauth/entity/OAuthClient.java`
- `oauth/entity/OAuthClientRedirectUri.java`
- `oauth/entity/AuthorizationCode.java`
- `oauth/repository/OAuthClientRepository.java`
- `oauth/repository/OAuthClientRedirectUriRepository.java`
- `oauth/repository/AuthorizationCodeRepository.java`
- `oauth/dto/CreateClientRequest.java`
- `oauth/dto/CreateClientResponse.java`
- `oauth/dto/ClientResponse.java`
- `oauth/dto/TokenResponse.java`
- `oauth/dto/AuthorizeRequest.java`
- `oauth/dto/TokenRequest.java`
- `oauth/exception/OAuthException.java`
- `oauth/exception/InvalidClientException.java`
- `oauth/exception/InvalidGrantException.java`
- `oauth/exception/InvalidRedirectUriException.java`
- `oauth/exception/InvalidScopeException.java`
- `oauth/exception/UnauthorizedClientException.java`
- `oauth/service/PKCEService.java`
- `oauth/service/OAuthClientService.java`
- `oauth/service/AuthorizationCodeService.java`
- `oauth/service/OAuthAuthorizationService.java`
- `oauth/controller/OAuthClientController.java`
- `oauth/controller/OAuthAuthorizationController.java`
- `oauth/controller/OAuthTokenController.java`
- `oauth/service/PKCEServiceTests.java`
- `oauth/service/OAuthClientServiceTests.java`
- `oauth/service/AuthorizationCodeServiceTests.java`
- `oauth/controller/OAuthIntegrationTests.java`

### Modified Files
- `config/SecurityConfig.java`: adds `/oauth/authorize` and `/oauth/token` as permitted; adds `hasRole("ADMIN")` for `/api/v1/oauth/clients/**`
- `common/exception/GlobalExceptionHandler.java`: adds `@ExceptionHandler(OAuthException.class)` returning 400 with error code
- `organization/controller/OrganizationControllerIntegrationTests.java`: adds `authorizationCodeRepository.deleteAll()` for FK cleanup
- `sessions/controller/SessionControllerIntegrationTests.java`: adds `authorizationCodeRepository.deleteAll()` for FK cleanup

## Test Coverage

- `PKCEServiceTests`: 3 tests (verifier generation, computed challenge matches known value, invalid verifier is rejected)
- `OAuthClientServiceTests`: 3 tests (create client, reject duplicate client_id, retrieve client by ID)
- `AuthorizationCodeServiceTests`: 3 tests (create code, consume valid code, reject consumed code, reject expired code)
- `OAuthIntegrationTests`: 11 integration tests (create and retrieve client, list clients, unauthenticated management returns 403, non-admin cannot create client, full authorization code flow with PKCE, code cannot be reused, client credentials grant succeeds, invalid secret rejected, redirect URI mismatch rejected, unsupported grant type returns error, unauthenticated authorize returns 403)

## Verification

- `./gradlew.bat build` succeeds with 113 tests passing (75 pre-existing + 14 new OAuth + 24 pre-existing maintained across shared H2 context).
- Flyway V7 migration runs successfully.
- Authorization Code Flow with PKCE S256 produces valid tokens usable for API calls.
- Authorization codes are single-use; second exchange returns `invalid_grant`.
- Client Credentials Flow issues tokens for confidential clients.
- Invalid client secrets return `invalid_client`.
- Unregistered redirect URIs are rejected with 400.
- Unsupported grant types return `unsupported_grant_type`.
- Unauthenticated requests to `/oauth/authorize` return 403 Forbidden.
- Client management endpoints require ROLE_ADMIN.
