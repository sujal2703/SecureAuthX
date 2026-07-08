# SecureAuthX PRD

## Current Product Stage

SecureAuthX is in Sprint 07: Passkeys (WebAuthn/FIDO2).

The current product objective is passwordless authentication and passkey management using the WebAuthn API.

## Sprint 07 Scope

In scope:

- WebAuthn registration options generation (`POST /api/v1/passkeys/register/options`).
- WebAuthn registration verification with origin, RP ID, and challenge validation (`POST /api/v1/passkeys/register/verify`).
- WebAuthn authentication options generation with discoverable credential support (`POST /api/v1/passkeys/authenticate/options`).
- WebAuthn authentication verification with signature verification, RP ID hash, user verification flag, and monotonic counter enforcement (`POST /api/v1/passkeys/authenticate/verify`).
- JWT access token + refresh token issuance on successful passkey authentication.
- Session creation on passkey authentication.
- List registered passkeys (`GET /api/v1/passkeys`).
- Delete passkey with ownership enforcement (`DELETE /api/v1/passkeys/{id}`).
- COSE key parsing for EC2 (P-256, P-384, P-521) and RSA public keys.
- Challenge management with 5-minute expiry and single-use enforcement.
- Public passkey authentication endpoints (no JWT required for authenticate options/verify).
- All 136 existing tests pass.

Out of scope:

- OpenID Connect.
- Social login.
- MFA policies.
- Browser frontend integration or UI.
- FIDO2 CA / attestation verification beyond attestation type selection.
- Passkey backup/export.

## Completed Sprints

- **Sprint 00**: Project foundation — Docker, PostgreSQL, Redis, Flyway, OpenAPI, health checks, environment configuration, tests.
- **Sprint 01**: User registration — email/password validation, Argon2id hashing, duplicate detection, consistent error responses.
- **Sprint 02**: Login, JWT (RS256), token refresh with rotation, and logout.
- **Sprint 03**: Session and device management — CRUD sessions, user-agent parsing, current session tracking.
- **Sprint 04**: RBAC — roles, permissions, user-role-permission data model, `@PreAuthorize` enforcement.
- **Sprint 05**: Organizations — multi-tenancy foundation, personal orgs, org membership with OWNER/ADMIN/MEMBER roles.
- **Sprint 06**: OAuth 2.1 Authorization Server — Authorization Code Flow with PKCE S256, Client Credentials Flow, client management.
- **Sprint 07**: Passkeys — WebAuthn/FIDO2 passwordless authentication and registration.

## Success Criteria

Sprint 07 succeeds when passkey registration creates a credential securely, passkey authentication issues JWT tokens, duplicate challenge usage is rejected, expired challenges are rejected, origin and RP ID validation rejects mismatched requests, monotonic counter enforcement rejects replayed assertions, signature verification rejects forged assertions, all 136 tests pass, and documentation is current.
