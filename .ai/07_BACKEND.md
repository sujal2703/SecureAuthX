# SecureAuthX Backend Engineering Guide

Version: 1.0.0

Status: ACTIVE

---

# Purpose

This document defines backend architecture, package organization, coding conventions and implementation standards.

Every backend feature must follow this document.

---

# Technology Stack

Java 21+

Spring Boot 3.x

Gradle Kotlin DSL

PostgreSQL

Flyway

Redis

Spring Security

OpenAPI

JUnit

Mockito

Testcontainers

---

# Backend Philosophy

Backend code must be

- Secure
- Modular
- Testable
- Readable
- Production Ready

Avoid unnecessary complexity.

---

# Package Structure

Every module follows

module/

controller/

service/

repository/

entity/

dto/

mapper/

validation/

exception/

config/

Never place unrelated classes together.

---

# Controller Rules

Controllers are responsible only for

- Receiving requests
- Validation
- Calling services
- Returning responses

Controllers must NEVER

- Access repositories

- Contain business logic

- Execute SQL

- Perform authentication logic

---

# Service Rules

Services contain business logic.

Services may

- Access repositories

- Call other services

- Publish domain events

Services must never

- Return entities directly

- Depend on HTTP

- Depend on controllers

---

# Repository Rules

Repositories communicate with the database only.

Repositories should

- Use Spring Data JPA

- Use parameterized queries

Repositories must never

- Validate business rules

- Call external services

---

# DTO Rules

Every endpoint requires

Request DTO

Response DTO

Never expose entities directly.

Validate DTOs before business logic executes.

---

# Entity Rules

Entities represent persistence only.

Entities should remain simple.

Avoid business logic inside entities unless truly domain-driven.

---

# Mapper Rules

MapStruct is preferred when mappings become repetitive.

Simple mappings may be handwritten.

---

# Validation

Validate

- Email

- Password

- UUID

- Length

- Required Fields

Reject invalid input immediately.

---

# Exception Handling

Use centralized exception handling.

Return consistent JSON error responses.

Never expose stack traces.

---

# Configuration

Configuration belongs inside

config/

Examples

SecurityConfig

OpenApiConfig

RedisConfig

DatabaseConfig

CorsConfig

JacksonConfig

---

# Logging

Use SLF4J.

Log

- Startup

- Shutdown

- Login

- Registration

- Security Events

- Errors

Never log

- Passwords

- Tokens

- Secrets

---

# REST API

Every endpoint starts with

/api/v1/

Use proper HTTP methods

GET

POST

PUT

PATCH

DELETE

Return proper HTTP status codes.

---

# Dependency Rules

Allowed

Controller

↓

Service

↓

Repository

Forbidden

Controller

↓

Repository

Controller

↓

Database

Repository

↓

Controller

---

# Testing

Every Service

Unit Tests

Every Repository

Integration Tests

Critical Authentication Flow

End-to-End Tests

---

# Documentation

Whenever backend code changes

Update

- API documentation

- Database documentation

- Architecture documentation

- Project Memory

---

# Definition of Backend Complete

A backend feature is complete only when

✓ Compiles

✓ Tests Pass

✓ Documentation Updated

✓ Logging Added

✓ Validation Added

✓ Flyway Migration Added (if required)

✓ Security Reviewed

✓ OpenAPI Updated

---

# Backend Principle

Backend code should be understandable by a new engineer within minutes.

Complexity is a bug.

Readability is a feature.

