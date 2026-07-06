# SecureAuthX User Flows

## Current Stage

Sprint 01 includes user registration only.

## Developer Foundation Flow

1. Copy `.env.example` to `.env`.
2. Replace local secret values.
3. Run `docker compose up --build`.
4. Verify backend health at `/actuator/health`.
5. Verify OpenAPI at `/v3/api-docs` or `/swagger-ui`.

## Registration Flow

1. Client submits `POST /api/v1/auth/register` with email and password.
2. Backend validates email format and password strength.
3. Backend normalizes email to lowercase.
4. Backend rejects duplicate emails with `409 Conflict`.
5. Backend hashes the password with Argon2id.
6. Backend stores the user record.
7. Backend returns `201 Created` with user id, normalized email, and creation timestamp.

## Future Flows

Login, logout, password reset, email verification, sessions, RBAC, organizations, OAuth, OpenID Connect, and passkeys are future sprint work and must not be implemented during Sprint 01.
