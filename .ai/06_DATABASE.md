# SecureAuthX Database Standards

Version: 1.0.0

Status: ACTIVE

---

# Purpose

This document defines database architecture, ownership, migration strategy and performance rules for SecureAuthX.

The database is a core part of the platform and must evolve safely.

---

# Database Engine

Primary Database

PostgreSQL 17+

Character Set

UTF-8

Timezone

UTC

---

# Migration Strategy

Every schema modification MUST use Flyway.

Rules

- Never modify an existing migration after it has been committed.
- Every schema change requires a new versioned migration.
- Migrations must be repeatable and deterministic.
- Production databases must never be altered manually.

Migration naming format

V1__Initial_schema.sql

V2__Create_users_table.sql

V3__Create_sessions_table.sql

---

# Module Ownership

Each module owns its tables.

Examples

auth

- login_attempts
- password_resets
- email_verifications

users

- users
- user_profiles

sessions

- sessions
- refresh_tokens

audit

- audit_events

organizations

- organizations
- organization_members

risk

- risk_events

Only the owning module may directly modify its tables.

Other modules must communicate through services.

---

# Table Standards

Every table must contain

- id
- created_at
- updated_at

Use UUIDs where appropriate.

Use meaningful table names.

Avoid abbreviations.

---

# Naming Convention

Tables

snake_case

Columns

snake_case

Indexes

idx_<table>_<column>

Foreign Keys

fk_<table>_<reference>

Unique Constraints

uk_<table>_<column>

---

# Indexing

Create indexes for

- Foreign Keys
- Frequently searched columns
- Login identifiers (email)
- Session lookups
- Token lookups

Avoid unnecessary indexes.

---

# Constraints

Use

NOT NULL

UNIQUE

CHECK

FOREIGN KEY

where appropriate.

Database constraints are required even if validation exists in the application.

---

# Transactions

Business operations spanning multiple tables must execute within transactions.

Keep transactions as short as possible.

---

# Soft Deletes

Avoid deleting important business records.

Prefer soft deletes where audit history is required.

---

# Audit

Authentication events must never be lost.

Audit records should be immutable.

---

# Performance

Avoid

SELECT *

Avoid N+1 queries.

Always paginate large result sets.

Optimize before adding complexity.

---

# Backups

Production deployments must include

- Automated Backups
- Restore Testing
- Retention Policy

---

# Database Security

Never expose database credentials.

Use least-privilege accounts.

Encrypt backups.

Restrict production access.

---

# AI Rules

When creating database changes

Always

✓ Create Flyway migration

✓ Update documentation

✓ Add indexes

✓ Consider rollback strategy

✓ Review performance impact

Never

✗ Modify existing migrations

✗ Bypass Flyway

✗ Use destructive SQL without justification

---

# Database Principle

The database is a long-term asset.

Every schema change should be safe, reversible where practical, and well documented.

