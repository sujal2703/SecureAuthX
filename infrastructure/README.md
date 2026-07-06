# SecureAuthX Infrastructure

Sprint 00 infrastructure is defined at the repository root in `docker-compose.yml`.

## Services

- `backend`: Spring Boot API service.
- `postgres`: PostgreSQL 17 for durable relational data.
- `redis`: Redis 7 for cache and future session-adjacent state.

## Local Startup

Copy `.env.example` to `.env`, replace all secret values, then run:

```bash
docker compose up --build
```

Health endpoints:

- Backend actuator health: `http://localhost:8080/actuator/health`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui`

## Secret Handling

Do not commit `.env` files. Runtime secrets must be supplied through environment variables or a production secret manager.
