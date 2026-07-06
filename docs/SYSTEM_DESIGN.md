# SecureAuthX System Design

## Architecture

SecureAuthX uses a modular monolith architecture. During Sprint 00, the only executable application is the Spring Boot backend in `backend/server`.

Future feature modules must live under `com.secureauthx.server` using feature-first package boundaries and the layered pattern defined in the engineering handbook.

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

All other routes are denied by default until real authentication and authorization flows are implemented in later sprints. No authentication product flows are implemented in Sprint 00.

## Configuration

Runtime configuration is supplied through environment variables. Local examples are documented in `.env.example`.
