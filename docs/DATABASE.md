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

## Local Configuration

The backend reads PostgreSQL configuration from environment variables:

- `SECUREAUTHX_POSTGRES_HOST`
- `SECUREAUTHX_POSTGRES_PORT`
- `SECUREAUTHX_POSTGRES_DB`
- `SECUREAUTHX_POSTGRES_USER`
- `SECUREAUTHX_POSTGRES_PASSWORD`

Database credentials must never be committed to source control.
