# SecureAuthX Database

## Database Engine

SecureAuthX uses PostgreSQL 17 for relational persistence.

## Migration Strategy

All schema changes are managed by Flyway migrations under:

`backend/server/src/main/resources/db/migration`

Existing migrations must not be edited after they are committed. New schema changes require new versioned migrations.

## Current Migrations

### `V1__Initial_foundation.sql`

Creates the `pgcrypto` extension if it is not already present. This enables PostgreSQL-native UUID generation for future schema work without introducing authentication-domain tables before Sprint 00 is complete.

### `V2__Create_users_table.sql`

Creates the `users` table for Sprint 01 registration.

Columns:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `email VARCHAR(320) NOT NULL`
- `password_hash VARCHAR(255) NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Constraints and indexes:

- `uk_users_email` enforces unique email addresses.
- `chk_users_email_not_blank` rejects blank emails.
- `chk_users_email_normalized` requires stored emails to be lowercase.
- `chk_users_password_hash_not_blank` rejects blank password hashes.
- `idx_users_created_at` supports creation-time queries.

### `V3__Create_refresh_tokens_table.sql`

Creates the `refresh_tokens` table for Sprint 02 token management.

Columns:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`
- `token_hash VARCHAR(64) NOT NULL`
- `expires_at TIMESTAMPTZ NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- `revoked_at TIMESTAMPTZ` (null until revoked)

Constraints and indexes:

- `uk_refresh_tokens_token_hash` enforces unique token hashes.
- `idx_refresh_tokens_user_id` supports user-based queries.
- `idx_refresh_tokens_token_hash` supports token lookup.
- `idx_refresh_tokens_expires_at` supports expiry cleanup.

Security notes:

- Only the SHA-256 hash of each refresh token is stored. Plaintext tokens are never persisted.
- Revoked tokens remain in the database with a `revoked_at` timestamp for audit purposes.
- Expired tokens can be cleaned up periodically via `idx_refresh_tokens_expires_at`.

### `V4__Create_sessions_table.sql`

Creates the `sessions` table for Sprint 03 session and device management.

### `V5__Create_rbac_tables.sql`

Creates the RBAC tables for Sprint 04 role-based access control.

### `V7__Create_oauth_tables.sql`

Creates three OAuth 2.1 tables for Sprint 06 authorization server.

Tables:

- `oauth_clients` — registered OAuth 2.1 clients.
- `oauth_client_redirect_uris` — registered redirect URIs per client.
- `oauth_authorization_codes` — authorization codes with PKCE challenges.

Columns for `oauth_clients`:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `client_id VARCHAR(100) NOT NULL UNIQUE`
- `client_secret VARCHAR(255)` (Argon2id hash, nullable for public clients)
- `client_name VARCHAR(255) NOT NULL`
- `confidential BOOLEAN NOT NULL DEFAULT false`
- `enabled BOOLEAN NOT NULL DEFAULT true`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Columns for `oauth_client_redirect_uris`:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `client_id UUID NOT NULL REFERENCES oauth_clients(id) ON DELETE CASCADE`
- `redirect_uri VARCHAR(2048) NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Columns for `oauth_authorization_codes`:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `code VARCHAR(255) NOT NULL UNIQUE`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`
- `client_id UUID NOT NULL REFERENCES oauth_clients(id) ON DELETE CASCADE`
- `redirect_uri VARCHAR(2048) NOT NULL`
- `code_challenge VARCHAR(255) NOT NULL`
- `challenge_method VARCHAR(10) NOT NULL DEFAULT 'S256'`
- `expires_at TIMESTAMPTZ NOT NULL`
- `consumed BOOLEAN NOT NULL DEFAULT false`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Constraints and indexes:

- `idx_oauth_clients_client_id` unique index on `client_id`.
- `idx_oauth_client_redirect_uris_client_id` index for redirect URI lookups by client.
- `idx_oauth_authorization_codes_code` unique index on `code`.
- `idx_oauth_authorization_codes_user_id` index for user-based queries.
- `idx_oauth_authorization_codes_client_id` index for client-based queries.

Security notes:

- Client secrets are hashed with Argon2id via Spring Security's `PasswordEncoder` bean.
- Authorization codes are single-use (`consumed` flag) and expire after 10 minutes.
- PKCE code challenges use SHA-256 (S256 method). Plain challenge method is not allowed.
- Refresh tokens issued during authorization code grant use the same `refresh_tokens` table as user login.

### `V8__Create_passkey_tables.sql`

Creates the `passkeys` and `webauthn_challenges` tables for Sprint 07 WebAuthn/FIDO2 passkey support.

Tables:

- `passkeys` — stores WebAuthn public key credentials per user.
- `webauthn_challenges` — stores one-time challenge strings for registration and authentication ceremonies.

Columns for `passkeys`:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`
- `credential_id VARCHAR(1024) NOT NULL`
- `public_key BYTEA NOT NULL` (raw COSE-encoded public key bytes)
- `counter BIGINT NOT NULL DEFAULT 0` (monotonically increasing signature counter)
- `credential_type VARCHAR(50) NOT NULL DEFAULT 'public-key'`
- `aaguid VARCHAR(36)` (authenticator AAGUID)
- `device_name VARCHAR(255)` (user-provided device label)
- `backed_up BOOLEAN NOT NULL DEFAULT false`
- `transports VARCHAR(255)` (comma-separated transport hints: usb, internal, nfc, ble)
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Columns for `webauthn_challenges`:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `challenge VARCHAR(255) NOT NULL`
- `user_id UUID REFERENCES users(id) ON DELETE CASCADE` (nullable for anonymous authentication flows)
- `purpose VARCHAR(20) NOT NULL` (REGISTER or AUTHENTICATE)
- `expires_at TIMESTAMPTZ NOT NULL` (5-minute expiry matching PKCE challenge pattern)
- `used BOOLEAN NOT NULL DEFAULT false` (single-use tracking)
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Constraints and indexes:

- `idx_passkeys_credential_id` unique index on `credential_id`.
- `idx_passkeys_user_id` index for user-based passkey queries.
- `idx_webauthn_challenges_challenge` index for challenge lookup.
- `idx_webauthn_challenges_user_id` index for user-based challenge queries.

Security notes:

- Challenges are single-use (`used` flag) and expire after 5 minutes (matching the PKCE pattern from Sprint 06).
- Challenge purpose (REGISTER vs AUTHENTICATE) is enforced at the application layer.
- Public keys are stored as raw COSE CBOR bytes; parsed at runtime into `java.security.PublicKey`.

### `V9__Add_oidc_nonce_to_authorization_codes.sql`

Adds `nonce` and `scope` columns to `oauth_authorization_codes` for Sprint 08 OpenID Connect 1.0 support.

Changes:

- `oauth_authorization_codes.nonce VARCHAR(255)` — stores the nonce parameter from the OIDC authorization request. Included in the ID Token and returned to the RP for replay detection.
- `oauth_authorization_codes.scope VARCHAR(1000)` — stores the scope parameter from the authorization request. Used at token exchange time to determine whether an ID Token should be issued (when `scope` contains `openid`).

### `V6__Create_organizations_tables.sql`

Creates the organizations and organization_members tables for Sprint 05 multi-tenancy foundation.

Tables:

- `organizations` — defines organizations (personal and non-personal).
- `organization_members` — maps users to organizations with organization-level roles.

Columns for `organizations`:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `name VARCHAR(255) NOT NULL`
- `slug VARCHAR(255) NOT NULL UNIQUE`
- `is_personal BOOLEAN NOT NULL DEFAULT false`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Columns for `organization_members`:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`
- `role VARCHAR(20) NOT NULL DEFAULT 'MEMBER'`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Constraints and indexes:

- `idx_organizations_slug` unique index on slug.
- `idx_organization_members_organization_id` for member lookups by org.
- `idx_organization_members_user_id` for org lookups by user.
- `idx_organization_members_unique` unique constraint on (organization_id, user_id).

Organization-level roles: `OWNER`, `ADMIN`, `MEMBER`.

Tables:

- `roles` — defines available roles (USER, ADMIN).
- `permissions` — defines available permissions (USER_READ, USER_WRITE, SESSION_READ, SESSION_REVOKE, ROLE_READ, ROLE_WRITE).
- `user_roles` — maps users to roles.
- `role_permissions` — maps roles to permissions.

Columns for `roles`:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `name VARCHAR(100) NOT NULL UNIQUE`
- `description VARCHAR(500)`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Columns for `permissions`:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `name VARCHAR(100) NOT NULL UNIQUE`
- `description VARCHAR(500)`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Columns for `user_roles`:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`
- `role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Columns for `role_permissions`:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE`
- `permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`

Constraints and indexes:

- `idx_roles_name` unique index on role names.
- `idx_permissions_name` unique index on permission names.
- `idx_user_roles_user_id` and `idx_user_roles_role_id` for join lookups.
- `idx_user_roles_unique` unique constraint on (user_id, role_id).
- `idx_role_permissions_role_id` and `idx_role_permissions_permission_id` for join lookups.
- `idx_role_permissions_unique` unique constraint on (role_id, permission_id).

Seed data:

- Roles: `USER`, `ADMIN`
- Permissions: `USER_READ`, `USER_WRITE`, `SESSION_READ`, `SESSION_REVOKE`, `ROLE_READ`, `ROLE_WRITE`
- `USER` role receives `SESSION_READ`, `SESSION_REVOKE`, `ROLE_READ`
- `ADMIN` role receives all permissions

Columns:

- `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE`
- `refresh_token_id UUID REFERENCES refresh_tokens(id) ON DELETE CASCADE`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- `last_activity_at TIMESTAMPTZ NOT NULL DEFAULT now()`
- `expires_at TIMESTAMPTZ NOT NULL`
- `revoked_at TIMESTAMPTZ` (null until revoked)
- `ip_address VARCHAR(45)` (supports IPv4 and IPv6)
- `user_agent TEXT`
- `device_name VARCHAR(255)`
- `operating_system VARCHAR(100)`
- `browser VARCHAR(100)`

Constraints and indexes:

- `idx_sessions_user_id` supports user-based session queries.
- `idx_sessions_user_revoked_expires` indexes (`user_id`, `revoked_at`, `expires_at`) for active session lookups.

Notes:

- Each session tracks the device that created it via user-agent parsing (browser, OS, device name).
- The `refresh_token_id` FK links sessions to their refresh token for simultaneous revocation.
- Sessions auto-expire after 7 days.

## Security

- Plaintext passwords are never stored.
- Passwords are hashed with Argon2id through Spring Security.
- Refresh tokens are stored as SHA-256 hashes.
- Email addresses are normalized before persistence.

## Local Configuration

The backend reads PostgreSQL configuration from environment variables:

- `SECUREAUTHX_POSTGRES_HOST`
- `SECUREAUTHX_POSTGRES_PORT`
- `SECUREAUTHX_POSTGRES_DB`
- `SECUREAUTHX_POSTGRES_USER`
- `SECUREAUTHX_POSTGRES_PASSWORD`

Database credentials must never be committed to source control.
