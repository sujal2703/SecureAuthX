# SecureAuthX User Flows

## Current Stage

Sprint 04 includes role-based access control.

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
7. Backend assigns `ROLE_USER` to the new user.
8. Backend returns `201 Created` with user id, normalized email, and creation timestamp.

## Role and Permission Listing Flow

1. Client authenticates via login to obtain a JWT access token.
2. Client uses the Bearer JWT to call `GET /api/v1/roles` or `GET /api/v1/permissions`.
3. Backend validates the JWT, loads user authorities, and returns the requested data.
4. Responses contain only safe information (id, name, description) — no internal mappings.

## Future Flows

Password reset, email verification, organizations, OAuth, OpenID Connect, passkeys, role assignment endpoints, and developer portal are future sprint work.
