# SecureAuthX Project Memory

Version: 1.0.0

Status: ACTIVE

---

# Purpose

This document is the persistent memory of the SecureAuthX project.

Every AI agent MUST read this file before starting work.

Every completed sprint MUST update this file.

Never delete historical information.

Append new information where appropriate.

---

# Current Phase

Multi-Tenancy Foundation

---

# Current Sprint

Sprint 05

---

# Repository Status

ACTIVE DEVELOPMENT

---

# Completed Work

Engineering handbook created.

Repository structure established.

Spring Boot project initialized.

Basic project folders created.

Backend project imports successfully.

Sprint 00 completed on 2026-07-06.

Sprint 00 delivered Docker, Docker Compose, PostgreSQL 17, Redis 7, Flyway, environment-driven Spring Boot configuration, structured console logging, OpenAPI, actuator health endpoints, a backend Dockerfile, foundation tests, and foundation documentation.

Verification completed:

- `./gradlew.bat build` succeeds.
- Backend tests pass.
- `docker compose --env-file .env.example up --build -d` starts all services.
- PostgreSQL responds to `select 1`.
- Redis responds to `PONG`.
- Flyway schema history contains successful migration `1 - Initial foundation`.
- `/actuator/health` returns `UP`.
- `/v3/api-docs` returns SecureAuthX OpenAPI metadata.

Sprint 01 completed on 2026-07-06.

Sprint 01 delivered production-ready user registration only. It added the `auth` module package structure, `User` entity, `UserRepository`, registration DTOs, password strength validation, registration service, registration controller, Argon2id password hashing, consistent JSON error handling, OpenAPI annotations, and a Flyway users table migration.

Sprint 01 verification completed:

- `./gradlew.bat build` succeeds.
- Unit tests cover password strength validation and registration service behavior.
- Integration tests cover successful registration, duplicate email conflict, invalid request validation, Argon2id password hashing, and PostgreSQL persistence through Testcontainers.
- `docker compose --env-file .env.example up --build -d` starts the stack.
- Flyway schema history contains successful migration `2 - Create users table`.
- Registration returns `201 Created`.
- Duplicate email returns `409 Conflict`.
- Invalid request returns `400 Bad Request`.
- Password hashes are stored with the Argon2id format and plaintext passwords are not stored.

Sprint 02 completed on 2026-07-07.

Sprint 02 delivered a production-ready authentication engine. It added login, token refresh, and logout endpoints. It implemented RS256 JWT access tokens, refresh token persistence with SHA-256 hashing, token rotation on refresh, and revocable refresh tokens. Added `JwtService` for JWT creation and validation, `AuthenticationService` for login/refresh/logout orchestration, `AuthenticationController` with three endpoints, `RefreshToken` entity, `RefreshTokenRepository`, login and refresh token DTOs, `InvalidCredentialsException`, `InvalidTokenException`, and a Flyway refresh tokens table migration.

Sprint 02 verification completed:

- `./gradlew.bat build` succeeds.
- Unit tests cover JWT token creation and validation, login with valid/invalid credentials, refresh with valid/revoked/expired/non-existent tokens, and logout with existing and non-existent tokens.
- Integration tests cover full login flow, invalid credential rejection, token refresh with rotation, revoked token rejection, logout with revocation, and validation error responses using Testcontainers and PostgreSQL.
- `docker compose --env-file .env.example up --build -d` starts the stack.
- Flyway schema history contains successful migration `3 - Create refresh tokens table`.
- Login returns `200 OK` with access and refresh tokens for valid credentials.
- Login returns `401 Unauthorized` for invalid credentials without distinguishing email vs password.
- Refresh returns `200 OK` with rotated tokens for a valid refresh token.
- Refresh returns `401 Unauthorized` for revoked, expired, or non-existent tokens.
- Logout returns `204 No Content` and revokes the token.
- Refresh tokens are stored as SHA-256 hashes and plaintext tokens are never stored.

Sprint 03 completed on 2026-07-07.

Sprint 03 delivered production-ready session and device management. It added five session endpoints (list, current, revoke by id, revoke current, revoke all), automatic session creation on login and refresh, device fingerprinting via User-Agent parsing (browser, OS, device name), session ID embedded in JWT access tokens for current-session identification, and simultaneous session-refresh token revocation. Added `SessionService`, `SessionController`, `Session` entity, `SessionRepository`, `SessionResponse` DTO, `UserAgentParser`, `SessionNotFoundException`, `JwtAuthenticationFilter` for Bearer token validation, and a Flyway sessions table migration. Modified `AuthenticationService` and `AuthenticationController` to accept and forward device/network context (IP address, User-Agent).

Sprint 03 verification completed:

- `./gradlew.bat build` succeeds (51 total tests: 28 unit + 22 integration + 1 API doc).
- Unit tests cover `UserAgentParser` browser/OS/device parsing with 5 UA scenarios, `SessionService` create/list/revoke/revoke-all operations with user ownership checks.
- Integration tests cover login creates session, list sessions returns device info, get current session, revoke single/current/all sessions, 404 for non-existent session, forbidden without auth, and multiple logins create multiple sessions.
- Access tokens include `sessionId` claim.
- Sessions are revoked together with their associated refresh tokens.
- `docker compose --env-file .env.example up --build -d` starts the stack.
- Flyway schema history contains successful migration `4 - Create sessions table`.

Sprint 04 completed on 2026-07-07.

Sprint 04 delivered production-ready role-based access control. It added RBAC tables (roles, permissions, user_roles, role_permissions) via Flyway V5 with seed data for ROLE_USER, ROLE_ADMIN, and six permissions. Implemented `Role`, `Permission`, `UserRole`, `RolePermission` entities with repositories using `JOIN FETCH` queries. Added `RoleService`, `PermissionService`, `UserAuthorityService` (loads roles/permissions as GrantedAuthority objects), `RoleController`, and `PermissionController` with read-only endpoints. Integrated with Spring Security via `@EnableMethodSecurity` and `@PreAuthorize`. Modified `RegistrationService` to auto-assign ROLE_USER on registration. Modified `JwtAuthenticationFilter` to load authorities on every authenticated request via `UserAuthorityService`. Modified `SecurityConfig` with `@EnableMethodSecurity` and RBAC endpoint access rules.

Sprint 04 verification completed:

- `./gradlew.bat build` succeeds (62 total tests: 33 unit + 28 integration + 1 API doc).
- Unit tests cover `RoleService` listing, `PermissionService` listing, and `UserAuthorityService` role/permission loading.
- Integration tests cover authenticated role listing, authenticated permission listing, forbidden for unauthenticated requests, and safe response structure (no internal mappings).
- `RegistrationService` tests verify ROLE_USER assignment on registration.
- Flyway schema history contains successful migration `5 - Create RBAC tables`.
- `docker compose --env-file .env.example up --build -d` starts the stack.

Sprint 05 completed on 2026-07-07.

Sprint 05 delivered organizations and multi-tenancy foundation. It added organizations and organization_members tables via Flyway V6 with OWNER, ADMIN, MEMBER roles. Implemented `Organization`, `OrganizationMember`, `OrganizationRole` entities with repositories using `JOIN FETCH` queries. Added `OrganizationService` (create personal org, create org, list, get current, update with OWNER/ADMIN check), `OrganizationController` with four endpoints (GET list, GET current, POST create, PATCH update), DTOs, and org-specific exceptions. Modified `RegistrationService` to auto-create a personal organization with OWNER role on registration. Modified `SecurityConfig`, `GlobalExceptionHandler`, and test cleanup to support shared H2 context across integration tests.

Sprint 05 verification completed:

- `./gradlew.bat build` succeeds (75 total tests: 38 unit + 36 integration + 1 API doc).
- Unit tests cover `OrganizationService` create personal org, create org, list, get current, update as OWNER, update as MEMBER throws.
- Integration tests cover list orgs, get current org, create org, update org, unauthenticated forbidden, member cannot update, registration creates personal org.
- Flyway schema history contains successful migration `6 - Create organizations tables`.
- `docker compose --env-file .env.example up --build -d` starts the stack (pending verification).

---

# Work In Progress

No active sprint implementation is in progress. Awaiting review before Sprint 06.

---

# Pending Work

Sprint 03

Session Management

Sprint 04

Authorization (RBAC)

Sprint 05

Organizations & Multi-Tenancy Foundation

Sprint 06

OAuth 2.1

Sprint 07

OpenID Connect

Sprint 08

Passkeys

Sprint 09

AI Risk Engine

Sprint 10

Developer Portal

Sprint 11

Admin Portal

Sprint 12

Production Deployment

---

# Test Results

Total tests after Sprint 05: 75 (38 unit, 36 integration, 1 OpenAPI documentation test).

Test breakdown:

- `src/test/java/com/secureauthx/server/auth/service/AuthenticationServiceTests.java`: 13 tests (login, refresh, logout, token rotation)
- `src/test/java/com/secureauthx/server/auth/jwt/JwtServiceTests.java`: 4 tests (token creation, validation, tamper detection)
- `src/test/java/com/secureauthx/server/auth/controller/AuthenticationControllerIntegrationTests.java`: 7 tests (register, login, refresh, logout, validation errors)
- `src/test/java/com/secureauthx/server/auth/service/RegistrationServiceTests.java`: 2 tests (email normalization, duplicate rejection)
- `src/test/java/com/secureauthx/server/sessions/service/SessionServiceTests.java`: 5 tests (create, list, revoke by id, revoke all, ownership check)
- `src/test/java/com/secureauthx/server/sessions/service/UserAgentParserTests.java`: 5 tests (Chrome/Windows, Firefox/macOS, Safari/iOS, Edge/Android, null, empty)
- `src/test/java/com/secureauthx/server/sessions/controller/SessionControllerIntegrationTests.java`: 7 tests (login creates session, list, current, revoke by id, revoke current, revoke all, multiple logins, not found, forbidden)
- `src/test/java/com/secureauthx/server/authorization/service/RoleServiceTests.java`: 2 tests (returns all roles, returns empty list)
- `src/test/java/com/secureauthx/server/authorization/service/PermissionServiceTests.java`: 2 tests (returns all permissions, returns empty list)
- `src/test/java/com/secureauthx/server/authorization/service/UserAuthorityServiceTests.java`: 2 tests (loads role and permission authorities, returns empty for user with no roles)
- `src/test/java/com/secureauthx/server/authorization/controller/AuthorizationControllerIntegrationTests.java`: 5 tests (user can list roles, user can list permissions, unauthenticated returns forbidden for roles and permissions, response does not expose internal mappings)
- `src/test/java/com/secureauthx/server/organization/service/OrganizationServiceTests.java`: 6 tests (create personal org, create org, list orgs, get current org, update as OWNER, update as MEMBER throws)
- `src/test/java/com/secureauthx/server/organization/controller/OrganizationControllerIntegrationTests.java`: 8 tests (list orgs, get current, create org, update, unauthenticated forbidden, member cannot update, registration creates personal org)
- `src/test/java/com/secureauthx/server/config/OpenApiConfigTests.java`: 1 test (OpenAPI documentation)
- `src/test/java/com/secureauthx/server/ServerApplicationTests.java`: 1 test (context loads)

---

# Architecture Decisions

Architecture Style

Modular Monolith

Backend

Spring Boot

Frontend

Next.js

Database

PostgreSQL

Cache

Redis

Migration Tool

Flyway

API Style

REST

Authentication Strategy

JWT + Refresh Tokens

JWT Algorithm

RS256 (asymmetric RSA with SHA-256)

JWT Key Size

2048 bits

Access Token Expiration

15 minutes (configurable via `SECUREAUTHX_JWT_ACCESS_TOKEN_EXPIRATION_MINUTES`)

Access Token Claims

`sub` (user id UUID), `email`, `sessionId` (UUID of the current session)

Authorization Strategy

Permission-based RBAC with roles and fine-grained permissions. Roles are stored with `ROLE_` prefix for `hasRole()` support. Permissions are stored as-is for `hasAuthority()` support. Authorities are loaded from the database on every request via `UserAuthorityService`.

Refresh Token Expiration

7 days (configurable via `SECUREAUTHX_JWT_REFRESH_TOKEN_EXPIRATION_DAYS`)

Refresh Token Storage

SHA-256 hash of the token value. Plaintext tokens are never stored.

Token Rotation

Each refresh invalidates the previous refresh token.

Password Hashing

Argon2id

Registration Password Encoder

Spring Security `Argon2PasswordEncoder` with Bouncy Castle provider support

---

# Known Issues

No known blocking issues.

Non-blocking improvement identified: Docker backend image builds currently download Gradle dependencies during image build. This is acceptable for Sprint 00 but should be optimized with better Docker layer caching in a future infrastructure refinement.

Ephemeral JWT key pair: When `SECUREAUTHX_JWT_PRIVATE_KEY` and `SECUREAUTHX_JWT_PUBLIC_KEY` are not set, the application generates an ephemeral RSA key pair on startup. Access tokens signed with this key become invalid after a restart. This is acceptable for development but production deployments must configure persistent keys via those environment variables.

If new issues arise they must be recorded here.

---

# Documentation Status

Engineering Handbook

ACTIVE

Architecture

UPDATED FOR SPRINT 05

Database

UPDATED FOR SPRINT 05

API

UPDATED FOR SPRINT 05

---

# AI Instructions

Before implementing any feature

Read this document.

After completing any feature

Update

Completed Work

Pending Work

Known Issues

Architecture Decisions (if changed)

Documentation Status

Current Sprint

This document is the long-term memory of SecureAuthX.

Never remove historical project information.
