# SecureAuthX

**Enterprise-grade open-source authentication and identity platform.**

[![CI](https://github.com/anomalyco/SecureAuthX/actions/workflows/ci.yml/badge.svg)](https://github.com/anomalyco/SecureAuthX/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

---

## Features

- **Authentication**: Email/password registration and login with Argon2id hashing
- **JWT (RS256)**: Asymmetric token signing with configurable expiry
- **Refresh Tokens**: SHA-256 hashed storage, rotation, and revocation
- **Sessions**: Device-aware session management with User-Agent parsing
- **RBAC**: Roles and permissions with `@PreAuthorize` enforcement
- **Organizations**: Multi-tenancy with personal orgs, OWNER/ADMIN/MEMBER roles
- **OAuth 2.1**: Authorization Code Flow with PKCE (S256 mandatory) + Client Credentials
- **OpenID Connect 1.0**: ID Tokens, UserInfo, Discovery, JWKS
- **Passkeys (WebAuthn/FIDO2)**: Passwordless authentication with COSE key parsing
- **Developer Portal**: Projects, API keys, secret rotation, usage analytics, rate limits
- **Admin Portal**: Dashboard, audit logs, announcements, system settings, security incidents
- **Observability**: Actuator health/liveness/readiness, Micrometer + Prometheus
- **Structured Logging**: MDC with requestId, userId, sessionId, correlation filter
- **Security Hardening**: CSP, HSTS, CORS, security headers
- **Rate Limiting**: Redis-backed, configurable per-endpoint limits

---

## Architecture

Modular monolith with feature-first package organization.

```
backend/server/
├── src/main/java/com/secureauthx/server/
│   ├── auth/         # Registration, login, logout, token refresh
│   ├── sessions/     # Session and device management
│   ├── authorization/ # RBAC (roles, permissions)
│   ├── organization/ # Multi-tenancy
│   ├── oauth/        # OAuth 2.1 Authorization Server
│   ├── passkey/      # WebAuthn/FIDO2 passkeys
│   ├── oidc/         # OpenID Connect 1.0 Provider
│   ├── developer/    # Developer Portal
│   ├── admin/        # Admin Portal
│   ├── common/       # Shared exceptions, DTOs
│   └── config/       # Security, JWT, CORS, rate limiting
└── src/main/resources/
    └── db/migration/  # Flyway migrations (V1-V11)
```

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Runtime | Java 21 (Temurin) |
| Framework | Spring Boot 3.5 |
| Build | Gradle (Kotlin DSL) |
| Database | PostgreSQL 17 |
| Cache | Redis 7 |
| Migrations | Flyway |
| JWT | JJWT (RS256) |
| Observability | Micrometer, Prometheus |
| API Docs | Springdoc OpenAPI |
| Testing | JUnit 5, Mockito, Testcontainers |

---

## Quick Start

1. **Clone and configure**
   ```bash
   cp .env.example .env
   # Edit .env with your local values
   ```

2. **Start the stack**
   ```bash
   docker compose --env-file .env up --build -d
   ```

3. **Verify health**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

4. **Explore the API**
   - Swagger UI: [http://localhost:8080/swagger-ui](http://localhost:8080/swagger-ui)
   - OpenAPI: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Docker

```bash
# Build and start all services
docker compose --env-file .env up --build -d

# View logs
docker compose logs -f

# Stop all services
docker compose down

# Rebuild a single service
docker compose build backend
```

The stack includes PostgreSQL 17, Redis 7, and the backend application with health checks and dependency ordering.

---

## Testing

```bash
cd backend/server
./gradlew.bat test       # Run all tests
./gradlew.bat build      # Full build with tests
```

**209 tests** covering authentication, sessions, RBAC, organizations, OAuth 2.1, passkeys, OIDC, developer portal, admin portal, and production readiness features.

---

## API Documentation

Full API documentation is maintained in [docs/API_SPEC.md](docs/API_SPEC.md). All product APIs use `/api/v1/`.

### Public Endpoints
- `GET /actuator/health` — Health check
- `POST /api/v1/auth/register` — User registration
- `POST /api/v1/auth/login` — User login
- `POST /api/v1/auth/refresh` — Token refresh
- `POST /api/v1/auth/logout` — Logout
- `GET /.well-known/openid-configuration` — OIDC Discovery
- `GET /.well-known/jwks.json` — JWKS endpoint

### Authenticated Endpoints
- `GET /api/v1/sessions` — List sessions
- `GET /api/v1/roles` — List roles
- `POST /api/v1/developer/projects` — Create developer project

### Admin Endpoints (requires ROLE_ADMIN)
- `GET /api/v1/admin/dashboard` — Platform dashboard
- `GET /api/v1/admin/audit` — Browse audit logs
- `POST /api/v1/admin/announcements` — Create announcement

---

## Profiles

| Profile | Purpose |
|---------|---------|
| `local` | Default local development with relaxed settings |
| `dev` | Development with debug logging and detailed health |
| `test` | Test profile (H2 in-memory, Redis disabled) |
| `prod` | Production with performance tuning and startup validation |

Set via `SECUREAUTHX_PROFILE` environment variable.

---

## Environment Variables

Key configuration via environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SECUREAUTHX_POSTGRES_*` | PostgreSQL connection | — |
| `SECUREAUTHX_REDIS_*` | Redis connection | — |
| `SECUREAUTHX_JWT_PRIVATE_KEY` | RSA private key (base64 PKCS8) | Ephemeral (dev only) |
| `SECUREAUTHX_JWT_PUBLIC_KEY` | RSA public key (base64 X.509) | Ephemeral (dev only) |
| `SECUREAUTHX_RATE_LIMITING_ENABLED` | Enable Redis rate limiting | `false` |
| `SECUREAUTHX_CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | `http://localhost:3000,http://localhost:8080` |
| `SECUREAUTHX_PROFILE` | Active Spring profile | `local` |

See [.env.example](.env.example) for all variables.

---

## Project Structure

```
.ai/            — Engineering handbook and sprint tracking
docs/           — Architecture, API, database, deployment docs
backend/        — Spring Boot backend application
frontend/       — Next.js application (placeholder)
infrastructure/ — Infrastructure configs (placeholder)
```

---

## Roadmap

- [x] Sprint 00: Foundation — Docker, PostgreSQL, Redis, Flyway, OpenAPI
- [x] Sprint 01: User Registration
- [x] Sprint 02: Login, JWT, Refresh Tokens
- [x] Sprint 03: Session and Device Management
- [x] Sprint 04: RBAC Roles and Permissions
- [x] Sprint 05: Organizations and Multi-Tenancy
- [x] Sprint 06-09: OAuth 2.1, OIDC, Passkeys
- [x] Sprint 10: Developer Portal
- [x] Sprint 11: Admin Portal
- [x] Sprint 12: Production Readiness (Release Candidate)

---

## License

[MIT](LICENSE)

Copyright (c) 2026 SecureAuthX
