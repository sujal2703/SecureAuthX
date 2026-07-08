# SecureAuthX PRD

## Current Product Stage

SecureAuthX is in Sprint 10: Developer Portal.

The current product objective is implementing a self-service Developer Portal API that allows developers to create projects, manage API keys, rotate OAuth client secrets, view usage analytics, and configure rate limits.

## Sprint 10 Scope

In scope:

- Developer project CRUD (create, list, get, update, delete) — each project belongs to a user and can optionally link an OAuth client.
- API key management (create, list, revoke) — keys are SHA-256 hashed before storage; plaintext shown only at creation.
- OAuth client secret rotation — generates a new random secret, hashes with Argon2id, updates the linked client; old secret immediately invalid.
- Daily usage analytics per project (request count, success/failure counts, average latency, token exchanges, userinfo requests).
- Rate limit management CRUD (set, get, delete) — stored but not enforced at runtime.
- User isolation: users may only manage their own projects; admins can access all projects.
- All 183 tests pass (147 pre-existing + 36 new developer portal tests).

Out of scope:

- Rate limit enforcement at runtime.
- Usage analytics aggregation beyond daily records.
- Frontend/UI for the developer portal (backend API only).
- API key usage tracking in request filters (tracking endpoints exist but are not wired into request processing).
- Webhook management.
- Audit logs.

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
- **Sprint 10**: Developer Portal — project management, API keys, secret rotation, usage analytics, rate limits.

## Success Criteria

Sprint 10 succeeds when developers can create and manage projects via REST API, API keys are created with SHA-256 hashed storage and plaintext returned only once, OAuth client secrets can be rotated with immediate invalidation of the old secret, daily usage analytics are returned for specified date ranges, rate limit configuration can be created, read, and deleted, users are isolated to their own projects, all 183 tests pass, and documentation is current.
