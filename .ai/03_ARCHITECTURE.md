# SecureAuthX Architecture

Version: 1.0.0

Status: ACTIVE

---

# Architecture Style

SecureAuthX uses a Modular Monolith architecture.

Reason

- Easier development
- Easier testing
- Easier deployment
- Clear module boundaries
- Future migration to microservices if required

No feature should be implemented as an independent microservice during the MVP.

---

# High Level Architecture

Client

↓

Frontend (Next.js)

↓

REST API

↓

Application Layer

↓

Domain Layer

↓

Persistence Layer

↓

PostgreSQL

Redis is used for caching and session-related data.

---

# Backend Structure

backend/

server/

src/

main/

java/

com/

secureauthx/

server/

auth/

users/

sessions/

credentials/

organizations/

roles/

permissions/

audit/

notifications/

oauth/

oidc/

risk/

common/

config/

---

# Layer Rules

Each module should follow

Controller

↓

Service

↓

Repository

↓

Database

Business logic belongs ONLY inside Services.

Controllers must never contain business logic.

Repositories must never contain business logic.

---

# Module Responsibilities

auth

Responsible for

- Registration
- Login
- Logout
- Password Reset
- Email Verification

users

Responsible for

- User profile
- User settings
- User lifecycle

sessions

Responsible for

- Refresh Tokens
- Session lifecycle
- Logout everywhere

credentials

Responsible for

- Password Hashing
- Passkeys
- Future PIN
- Future Pattern

audit

Responsible for

- Audit Logs
- Security Events

organizations

Responsible for

- Teams
- Organizations
- Membership

roles

Responsible for

- RBAC

permissions

Responsible for

- Permission management

notifications

Responsible for

- Email
- Future SMS
- Future Push

oauth

Responsible for

OAuth 2.1

oidc

Responsible for

OpenID Connect

risk

Responsible for

AI Risk Engine

Behavior Analysis

Device Fingerprinting

Impossible Travel

Adaptive Authentication

---

# Allowed Dependencies

Modules may communicate only through public service interfaces.

Never directly access another module's repository.

Never directly access another module's database tables.

---

# Common Package

common/

Contains

Exceptions

Utilities

Constants

Shared DTOs

Validators

Base Classes

---

# Config Package

config/

Contains

Security Configuration

Database Configuration

OpenAPI Configuration

Redis Configuration

CORS Configuration

Jackson Configuration

---

# Package Naming Rules

Use lowercase package names.

Feature-first organization.

Avoid generic packages like

utils

helpers

misc

---

# Database Ownership

Each module owns its own tables.

Only the owning module may modify those tables.

Other modules must use services.

---

# API Rules

All REST endpoints begin with

/api/v1/

Future breaking changes require

/api/v2/

Never break an existing API without versioning.

---

# Documentation Rules

Every new module requires

Architecture update

Database update

API update

Project Memory update

---

# Future Architecture

When scaling becomes necessary

Modules may be extracted into independent services.

Until then

Remain a Modular Monolith.

Do not prematurely optimize.

---

# Architecture Principle

Simple

Predictable

Modular

Secure

Maintainable

Scalable

These principles override implementation convenience.

