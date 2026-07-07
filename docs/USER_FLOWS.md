# SecureAuthX User Flows

## Current Stage

Sprint 05 includes organizations and multi-tenancy foundation.

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
8. Backend creates a personal organization with the user as `OWNER`.
9. Backend returns `201 Created` with user id, normalized email, and creation timestamp.

## Role and Permission Listing Flow

1. Client authenticates via login to obtain a JWT access token.
2. Client uses the Bearer JWT to call `GET /api/v1/roles` or `GET /api/v1/permissions`.
3. Backend validates the JWT, loads user authorities, and returns the requested data.
4. Responses contain only safe information (id, name, description) — no internal mappings.

## Organization Flows

### List Organizations

1. Client authenticates via login to obtain a JWT access token.
2. Client uses the Bearer JWT to call `GET /api/v1/organizations`.
3. Backend validates the JWT and returns all organizations the user belongs to, including the user's role within each.

### Get Personal Organization

1. Client authenticates via login to obtain a JWT access token.
2. Client uses the Bearer JWT to call `GET /api/v1/organizations/current`.
3. Backend validates the JWT and returns the user's personal organization.

### Create Organization

1. Client authenticates via login to obtain a JWT access token.
2. Client uses the Bearer JWT to call `POST /api/v1/organizations` with a name.
3. Backend creates the organization and adds the user as `OWNER`.
4. A URL-safe slug is auto-generated from the name.

### Update Organization

1. Client authenticates via login to obtain a JWT access token.
2. Client uses the Bearer JWT to call `PATCH /api/v1/organizations/{organizationId}` with updated fields.
3. Backend verifies the user is an `OWNER` or `ADMIN` of the organization.
4. Backend updates the organization and returns the updated record.

## Future Flows

Password reset, email verification, OAuth, OpenID Connect, passkeys, role assignment endpoints, invitation flows, member management, and developer portal are future sprint work.
