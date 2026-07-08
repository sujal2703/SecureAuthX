# SecureAuthX Implementation Plan

## Audit Summary

SecureAuthX is complete up to Sprint 12 (Production Readiness Release Candidate). All 12 sprints have been implemented and verified. The repository is ready for production deployment.

The `.ai` engineering handbook defines SecureAuthX as a modular monolith using Java, Spring Boot, Gradle Kotlin DSL, PostgreSQL, Redis, Flyway, Docker, JUnit, Mockito, OpenAPI, and a future Next.js frontend. It requires constructor injection, layered feature-first packages, environment variables for configuration, Flyway for every schema change, structured logging, tests, documentation updates, and project memory updates.

The `docs` directory exists but all files are currently empty. Foundation documentation must therefore be created as part of Sprint 00.

## Existing Architecture

- `backend/server` contains a generated Spring Boot application.
- The backend uses Gradle Kotlin DSL and currently declares Spring Web, Spring Security, Spring Data JPA, Flyway, PostgreSQL, and test dependencies.
- `frontend` exists but has no implementation yet.
- `infrastructure` exists but has no implementation yet.
- The intended architecture is a modular monolith with future feature packages under `com.secureauthx.server`.

## Existing Modules

- `backend/server`: executable backend service skeleton.
- `.ai`: engineering handbook, roadmap, current sprint, project memory, and definition of done.
- `docs`: empty documentation placeholders for product, system design, API, database, and user flows.
- `frontend`: empty placeholder.
- `infrastructure`: empty placeholder.

## Missing Sprint 00 Foundation

- Dockerfile for the backend.
- Root Docker Compose stack.
- PostgreSQL local service configuration.
- Redis local service configuration.
- Flyway baseline migration.
- Environment-driven Spring profiles/configuration.
- Logging configuration.
- OpenAPI configuration.
- Actuator health endpoint configuration.
- Environment variable template.
- Tests for foundation behavior.
- Documentation for architecture, API, database, and local development.
- Project memory updates after sprint tasks are completed.

## Security Concerns

- No explicit security filter chain exists yet.
- No environment template exists for secrets and runtime configuration.
- Baseline test execution currently fails because database configuration is incomplete.
- Generated build output and IDE files exist locally and must remain uncommitted.
- Docs do not yet describe secret handling, local dependency startup, or operational health checks.

## Technical Debt

- Root `.gitignore` is empty.
- `LICENSE` is empty.
- `docs` files are empty.
- `backend/server/HELP.md` is generated documentation and not project-specific.
- Java compilation targets Java 21 without forcing a specific local toolchain, allowing local JDK 23 and the Docker JDK 21 builder to compile consistently.

## Sprint 00 Execution Plan

Only the tasks in `.ai/12_CURRENT_SPRINT.md` are in scope.

1. Preserve the reviewed repository and handbook findings in this plan.
2. Add Docker and Docker Compose foundation for backend, PostgreSQL 17, and Redis.
3. Configure PostgreSQL, Redis, Flyway, Spring Boot environments, logging, OpenAPI, health endpoints, and environment variables.
4. Add a baseline Flyway migration without introducing authentication-domain tables.
5. Add tests that verify application context, health exposure, OpenAPI exposure, and security defaults.
6. Verify Gradle tests and build.
7. Verify Docker Compose startup, PostgreSQL connectivity, Redis connectivity, Flyway execution, application startup, OpenAPI, and health endpoint where the local environment permits.
8. Update `docs/*` with Sprint 00 foundation documentation.
9. Update `.ai/13_PROJECT_MEMORY.md` after completing Sprint 00 tasks.
10. Stop after Sprint 00 is complete and wait for review.

## Sprint Completion Status

### Sprint 00 — Foundation (2026-07-06)

- Backend build succeeds.
- Backend tests pass.
- Docker Compose builds and starts the backend, PostgreSQL, and Redis services.
- PostgreSQL 17 / Redis 7 are healthy.
- Flyway V1 baseline executes successfully.
- `/actuator/health` returns UP.
- `/v3/api-docs` returns SecureAuthX OpenAPI metadata.

### Sprint 01 — Registration (2026-07-06)

- Public registration endpoint with email/password validation.
- Argon2id password hashing, duplicate email rejection, consistent error responses.

### Sprint 02 — Login, JWT, Refresh, Logout (2026-07-06)

- RS256 JWT access tokens, refresh token rotation, SHA-256 hashed refresh tokens.
- Login, refresh, logout endpoints.

### Sprint 03 — Sessions (2026-07-07)

- Session CRUD, user-agent parsing, current session tracking.
- JWT carries `sessionId` claim.

### Sprint 04 — RBAC (2026-07-07)

- Roles/permissions/user_roles/role_permissions data model.
- `@PreAuthorize` enforcement, `JwtAuthenticationFilter` loads authorities from DB.

### Sprint 05 — Organizations (2026-07-07)

- Organizations and organization_members tables.
- Personal org auto-created on registration, OWNER/ADMIN/MEMBER roles.

### Sprint 06 — OAuth 2.1 (2026-07-08)

- Authorization Code Flow with PKCE S256, Client Credentials Flow.
- Client management (CRUD), Argon2id client secrets.
- OAuth token responses in snake_case convention.

### Sprint 07 — Passkeys (2026-07-08)

- WebAuthn registration and authentication flows.
- COSE key parser (EC2 P-256/P-384/P-521, RSA).
- Challenge management (5-minute expiry, single-use, purpose-based).
- Signature verification, monotonic counter, RP ID hash, user verification enforcement.
- JWT + refresh token issuance on passkey authentication.
- 6 endpoints: register options, register verify, authenticate options, authenticate verify, list, delete.
- All 136 tests pass.

### Sprint 08 — OpenID Connect 1.0 (2026-07-08)

- ID Token generation (RS256 signed JWT) when scope contains `openid`.
- Nonce parameter stored in authorization code and included in ID Token.
- UserInfo endpoint returning `sub` and `email` for Bearer token holders.
- OpenID Connect Discovery document at `/.well-known/openid-configuration`.
- JWKS endpoint at `/.well-known/jwks.json` exposing public RSA key only.
- OAuth backward compatibility: without `openid` scope, no ID Token is issued.
- Flyway V9: added `nonce` and `scope` columns to `oauth_authorization_codes`.
- 3 new endpoints: discovery, jwks, userinfo.
- 11 new tests: unit (3), integration (8).
- All 147 tests pass.
