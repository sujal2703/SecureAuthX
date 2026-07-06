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

Security notes:

- Plaintext passwords are never stored.
- Passwords are hashed with Argon2id through Spring Security.
- Email addresses are normalized before persistence.

## Local Configuration

The backend reads PostgreSQL configuration from environment variables:

- `SECUREAUTHX_POSTGRES_HOST`
- `SECUREAUTHX_POSTGRES_PORT`
- `SECUREAUTHX_POSTGRES_DB`
- `SECUREAUTHX_POSTGRES_USER`
- `SECUREAUTHX_POSTGRES_PASSWORD`

Database credentials must never be committed to source control.
