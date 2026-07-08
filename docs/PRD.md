# SecureAuthX PRD

## Current Product Stage

SecureAuthX is in Sprint 08: OpenID Connect 1.0 Provider.

The current product objective is implementing OpenID Connect 1.0 on top of the existing OAuth 2.1 Authorization Server, enabling SecureAuthX to act as an OpenID Provider (OP).

## Sprint 08 Scope

In scope:

- ID Token generation (RS256 signed JWT) issued alongside Access Tokens when `scope=openid` is requested.
- UserInfo endpoint (`GET /connect/userinfo`) returning `sub` and `email` for authenticated users.
- OpenID Connect Discovery document (`GET /.well-known/openid-configuration`) following the Discovery spec.
- JWKS endpoint (`GET /.well-known/jwks.json`) exposing the public RSA key.
- Nonce parameter support — stored in authorization code, included in ID Token.
- `openid` scope detection at token exchange time to conditionally issue ID Tokens.
- Backward compatibility: OAuth flows without `openid` scope behave identically to Sprint 06.
- All 147 existing tests pass (136 original + 11 new OIDC tests).

Out of scope:

- Dynamic Client Registration.
- RP-Initiated Logout, Front/Back Channel Logout.
- Session Management.
- Federation, Self-Issued OP, Identity Assurance.
- SAML, SCIM.

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

## Success Criteria

Sprint 08 succeeds when ID Tokens are correctly signed with RS256 and include `iss`, `sub`, `aud`, `exp`, `iat`, `auth_time`, and `nonce` claims, Discovery document returns all required metadata fields, JWKS endpoint exposes the public RSA key without exposing the private key, UserInfo returns user data for valid tokens and 401 for invalid/missing tokens, nonce is stored and included in the ID Token, OAuth flows without `openid` scope do not return ID Tokens, all 147 tests pass, and documentation is current.
