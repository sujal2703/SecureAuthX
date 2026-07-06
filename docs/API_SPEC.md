# SecureAuthX API Specification

## Current API Stage

Sprint 00 exposes only foundation endpoints. Product authentication APIs are intentionally out of scope until later sprints.

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

All non-foundation routes are denied by default until product authentication and authorization are implemented. No registration, login, token issuance, OAuth, OIDC, or RBAC endpoints exist in Sprint 00.

## Versioning

Future product APIs must use `/api/v1/`.
