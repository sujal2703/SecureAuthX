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

Core Authentication

---

# Current Sprint

Sprint 02

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

---

# Work In Progress

No active sprint implementation is in progress. Awaiting review before Sprint 03.

---

# Pending Work

Sprint 03

Session Management

Sprint 04

Authorization (RBAC)

Sprint 05

Organizations

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

UPDATED FOR SPRINT 02

Database

UPDATED FOR SPRINT 02

API

UPDATED FOR SPRINT 02

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
