# SecureAuthX System Design

## Architecture

SecureAuthX uses a modular monolith architecture. The executable application is the Spring Boot backend in `backend/server`.

Feature modules live under `com.secureauthx.server` using feature-first package boundaries and the layered pattern defined in the engineering handbook.

## Current Backend Modules

- `auth`: registration, login, token refresh, and logout. Contains controller, service, repository, entities, DTOs, mapper, validation, JWT service, and auth-specific exceptions.
- `common`: shared API error response and global exception handling.
- `config`: security and OpenAPI configuration.

## Runtime Components

- Backend: Spring Boot 3.x application.
- Database: PostgreSQL 17.
- Cache: Redis 7 (not yet used by auth; reserved for future session state).
- Migration tool: Flyway.
- API documentation: Springdoc OpenAPI.
- Health checks: Spring Boot Actuator.

## Local Development Topology

`docker-compose.yml` starts:

- `postgres`
- `redis`
- `backend`

The backend depends on healthy PostgreSQL and Redis containers before startup.

## Security Defaults

The foundation exposes only operational and documentation endpoints publicly:

- `/actuator/health`
- `/actuator/health/**`
- `/actuator/info`
- `/v3/api-docs`
- `/v3/api-docs/**`
- `/swagger-ui`
- `/swagger-ui/**`

Sprint 02 exposes the following authentication endpoints publicly:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

All other routes are denied by default until authorization flows are implemented in later sprints.

## Authentication Flow

1. **Registration**: User submits email and password via `POST /api/v1/auth/register`. Password is hashed with Argon2id. User record is created. No session or token is issued.

2. **Login**: User submits email and password via `POST /api/v1/auth/login`. Password is verified against the Argon2id hash. On success, an RS256-signed JWT access token and a refresh token are returned. The refresh token is stored as a SHA-256 hash.

3. **Token Refresh**: Client submits the refresh token via `POST /api/v1/auth/refresh`. The token hash is verified. If valid, the old refresh token is revoked and new access and refresh tokens are issued (token rotation).

4. **Logout**: Client submits the refresh token via `POST /api/v1/auth/logout`. The refresh token is revoked. Other sessions for the same user remain active.

## JWT Configuration

- Algorithm: RS256 (asymmetric RSA with SHA-256)
- Key size: 2048 bits
- Access token expiry: 15 minutes (configurable)
- Refresh token expiry: 7 days (configurable)
- Key pair: generated on startup, or configured via environment variables for production

## Configuration

Runtime configuration is supplied through environment variables. Local examples are documented in `.env.example`.
