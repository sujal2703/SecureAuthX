# SecureAuthX System Design

## Architecture

SecureAuthX uses a modular monolith architecture. During Sprint 01, the executable application is the Spring Boot backend in `backend/server`.

Feature modules live under `com.secureauthx.server` using feature-first package boundaries and the layered pattern defined in the engineering handbook.

## Current Backend Modules

- `auth`: registration endpoint, service, repository, entity, DTOs, mapper, validation, and auth-specific exceptions.
- `common`: shared API error response and global exception handling.
- `config`: security and OpenAPI configuration.

## Runtime Components

- Backend: Spring Boot 3.x application.
- Database: PostgreSQL 17.
- Cache: Redis 7.
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

Sprint 01 additionally exposes:

- `POST /api/v1/auth/register`

All other routes are denied by default until real authentication and authorization flows are implemented in later sprints. Registration does not create a session or issue tokens.

## Configuration

Runtime configuration is supplied through environment variables. Local examples are documented in `.env.example`.
