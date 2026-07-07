# SecureAuthX Current Sprint

Version: 1.0.0

Status: COMPLETE

Sprint Number

Sprint 02

Sprint Name

Authentication Engine

---

# Sprint Goal

Implement production-ready login, token refresh, and logout for SecureAuthX.

No MFA, OAuth, OIDC, RBAC, passkeys, or future roadmap items are included in this sprint.

---

# Objectives

Complete the following tasks in order.

1.

Review repository structure and engineering handbook.

2.

Create a Flyway migration for the `refresh_tokens` table.

3.

Create the JWT service package structure.

4.

Implement JWT key pair loading or generation.

5.

Implement `JwtService` for access token creation and validation.

6.

Implement `RefreshToken` entity.

7.

Implement `RefreshTokenRepository`.

8.

Implement login request and response DTOs.

9.

Implement refresh token request DTO.

10.

Implement `AuthenticationService` for login, refresh, and logout.

11.

Implement `AuthenticationController` for login, refresh, and logout endpoints.

12.

Add consistent JSON error responses for invalid credentials and invalid tokens.

13.

Return appropriate HTTP statuses for each endpoint.

14.

Document endpoints with OpenAPI annotations.

15.

Write unit tests.

16.

Write endpoint integration tests.

17.

Update API documentation.

18.

Update database documentation.

19.

Update system design documentation.

20.

Update project memory.

21.

Verify build, tests, Docker startup, and Flyway migration.

---

# Deliverables

At the end of Sprint 02 the repository must contain

✓ User login endpoint (`POST /api/v1/auth/login`)

✓ Token refresh endpoint (`POST /api/v1/auth/refresh`)

✓ Logout endpoint (`POST /api/v1/auth/logout`)

✓ Refresh tokens table migration (V3)

✓ RS256 JWT access token signing and validation

✓ Refresh token persistence with SHA-256 hashing

✓ Token rotation on refresh

✓ Revocable refresh tokens

✓ Argon2id password verification during login

✓ Validation and credential error responses

✓ OpenAPI documentation

✓ Passing unit and integration tests

---

# Not Included

The following features are NOT part of Sprint 02

✗ MFA

✗ OAuth 2.1

✗ OpenID Connect

✗ Passkeys

✗ Face Authentication

✗ Organizations

✗ RBAC

✗ Session Management (beyond refresh tokens)

✗ JWT authentication filter for protected routes (future sprint)

These belong to future sprints.

---

# Completion Criteria

Sprint 02 is complete only if

✓ Build succeeds

✓ Tests pass

✓ Login endpoint returns `200 OK` with access and refresh tokens for valid credentials

✓ Login returns `401 Unauthorized` for invalid email or password

✓ Login returns `400 Bad Request` for validation failures

✓ Login uses Argon2id to verify passwords

✓ Refresh endpoint returns `200 OK` with new tokens for a valid refresh token

✓ Refresh endpoint returns `401 Unauthorized` for revoked, expired, or non-existent tokens

✓ Refresh revokes the previous refresh token (rotation)

✓ Logout endpoint returns `204 No Content` and revokes the refresh token

✓ Logout does not invalidate unrelated refresh tokens

✓ Refresh tokens are stored as SHA-256 hashes, never in plaintext

✓ Flyway creates the refresh_tokens table successfully

✓ OpenAPI documents all three endpoints

✓ Documentation updated

✓ Project Memory updated

---

# AI Instructions

Implement Sprint 02 exactly as written.

Do not skip steps.

Do not begin Sprint 03 until Sprint 02 satisfies every completion criterion.

---

# Completion Record

Sprint 02 completed on 2026-07-07.

Verified:

- Backend build succeeds.
- Backend tests pass.
- Login endpoint returns `200 OK` with access and refresh tokens.
- Invalid credentials return `401 Unauthorized` without distinguishing email vs password.
- Validation failures return `400 Bad Request`.
- Passwords are verified with Argon2id.
- Refresh endpoint returns `200 OK` with rotated tokens.
- Revoked, expired, and non-existent refresh tokens return `401 Unauthorized`.
- Logout returns `204 No Content` and revokes the token.
- Refresh tokens are stored as SHA-256 hashes.
- Flyway executes migration `V3__Create_refresh_tokens_table.sql`.
- OpenAPI documents login, refresh, and logout.
- API, database, system design documentation and project memory are updated.
