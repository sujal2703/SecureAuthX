# SecureAuthX API Specification

## Current API Stage

Sprint 01 adds user registration. Login, JWT, OAuth, OIDC, passkeys, RBAC, and session creation remain out of scope.

## Public Foundation Endpoints

### `GET /actuator/health`

Returns Spring Boot actuator health status.

Expected success response:

```json
{
  "status": "UP"
}
```

### `GET /v3/api-docs`

Returns the OpenAPI document.

### `GET /swagger-ui`

Serves Swagger UI.

## Security

The registration endpoint is public. It creates a user account only and does not create a session or issue tokens.

All other non-foundation routes are denied by default until product authentication and authorization are implemented.

## Authentication Endpoints

### `POST /api/v1/auth/register`

Registers a user with email and password credentials.

Request:

```json
{
  "email": "developer@example.com",
  "password": "S3cureExample!2026"
}
```

Validation:

- `email` is required, must be valid, and must be 320 characters or fewer.
- `password` is required, must be 12 to 128 characters, and must include uppercase, lowercase, number, and special characters.

Success response: `201 Created`

```json
{
  "id": "49ba9128-32d8-4cf1-b136-3edb77a969f6",
  "email": "developer@example.com",
  "createdAt": "2026-07-06T17:00:00Z"
}
```

Error responses:

- `400 Bad Request` for validation failures.
- `409 Conflict` when the email is already registered.

Passwords are never returned by the API.

## Versioning

Future product APIs must use `/api/v1/`.
