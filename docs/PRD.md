# SecureAuthX PRD

## Current Product Stage

SecureAuthX is in Sprint 00: Project Foundation.

The current product objective is not end-user authentication. The objective is to establish a production-quality backend and local development foundation that future authentication work can safely build on.

## Sprint 00 Scope

In scope:

- Spring Boot backend foundation.
- Dockerized backend runtime.
- Docker Compose for backend, PostgreSQL, and Redis.
- Environment-variable based configuration.
- Flyway database migrations.
- Logging configuration.
- Actuator health endpoint.
- OpenAPI metadata and documentation endpoint.
- Foundation tests.

Out of scope:

- Registration.
- Login.
- JWT issuing or validation.
- OAuth or OpenID Connect.
- Passkeys.
- Organizations.
- RBAC.

## Success Criteria

Sprint 00 succeeds when the backend builds, tests pass, Docker Compose starts the foundation services, PostgreSQL and Redis are reachable, Flyway runs successfully, health responds, OpenAPI loads, and project documentation is current.
