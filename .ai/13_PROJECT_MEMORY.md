# SecureAuthX Project Memory

Version: 1.0.0

Status: ACTIVE

---

# Purpose

This document is the persistent memory of the SecureAuthX project.

Every AI agent MUST read this file before starting work.

Every completed sprint MUST update this file.

Never delete historical information.

Append new information where appropriate.

---

# Current Phase

Foundation

---

# Current Sprint

Sprint 00

---

# Repository Status

ACTIVE DEVELOPMENT

---

# Completed Work

Engineering handbook created.

Repository structure established.

Spring Boot project initialized.

Basic project folders created.

Backend project imports successfully.

Sprint 00 completed on 2026-07-06.

Sprint 00 delivered Docker, Docker Compose, PostgreSQL 17, Redis 7, Flyway, environment-driven Spring Boot configuration, structured console logging, OpenAPI, actuator health endpoints, a backend Dockerfile, foundation tests, and foundation documentation.

Verification completed:

- `./gradlew.bat build` succeeds.
- Backend tests pass.
- `docker compose --env-file .env.example up --build -d` starts all services.
- PostgreSQL responds to `select 1`.
- Redis responds to `PONG`.
- Flyway schema history contains successful migration `1 - Initial foundation`.
- `/actuator/health` returns `UP`.
- `/v3/api-docs` returns SecureAuthX OpenAPI metadata.

---

# Work In Progress

No active sprint implementation is in progress. Awaiting review before Sprint 01.

---

# Pending Work

Sprint 01

User Registration

Sprint 02

User Login

Sprint 03

Session Management

Sprint 04

Authorization (RBAC)

Sprint 05

Organizations

Sprint 06

OAuth 2.1

Sprint 07

OpenID Connect

Sprint 08

Passkeys

Sprint 09

AI Risk Engine

Sprint 10

Developer Portal

Sprint 11

Admin Portal

Sprint 12

Production Deployment

---

# Architecture Decisions

Architecture Style

Modular Monolith

Backend

Spring Boot

Frontend

Next.js

Database

PostgreSQL

Cache

Redis

Migration Tool

Flyway

API Style

REST

Authentication Strategy

JWT + Refresh Tokens

Password Hashing

Argon2id

---

# Known Issues

No known blocking issues.

Non-blocking improvement identified: Docker backend image builds currently download Gradle dependencies during image build. This is acceptable for Sprint 00 but should be optimized with better Docker layer caching in a future infrastructure refinement.

If new issues arise they must be recorded here.

---

# Documentation Status

Engineering Handbook

ACTIVE

Architecture

UPDATED FOR SPRINT 00

Database

UPDATED FOR SPRINT 00

API

UPDATED FOR SPRINT 00

---

# AI Instructions

Before implementing any feature

Read this document.

After completing any feature

Update

Completed Work

Pending Work

Known Issues

Architecture Decisions (if changed)

Documentation Status

Current Sprint

This document is the long-term memory of SecureAuthX.

Never remove historical project information.

