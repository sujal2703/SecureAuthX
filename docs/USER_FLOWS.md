# SecureAuthX User Flows

## Current Stage

Sprint 00 does not include user-facing authentication flows.

## Developer Foundation Flow

1. Copy `.env.example` to `.env`.
2. Replace local secret values.
3. Run `docker compose up --build`.
4. Verify backend health at `/actuator/health`.
5. Verify OpenAPI at `/v3/api-docs` or `/swagger-ui`.

## Future Flows

Registration, login, logout, password reset, email verification, sessions, RBAC, organizations, OAuth, OpenID Connect, and passkeys are future sprint work and must not be implemented during Sprint 00.
