# SecureAuthX AI Engineering Rules

Version: 1.0.0

Status: ACTIVE

---

# Purpose

This document defines how AI coding agents must behave while contributing to SecureAuthX.

These rules apply to every AI agent regardless of vendor.

Examples

- Codex

- ChatGPT

- Claude Code

- Cursor

- Gemini CLI

---

# Primary Objective

Build SecureAuthX as production-quality software.

Not a demo.

Not a tutorial.

Not a hackathon prototype.

Every change should improve the repository.

---

# Before Writing Code

Always

1. Read 00_START_HERE.md

2. Read the current engineering handbook

3. Read CURRENT_SPRINT.md

4. Read PROJECT_MEMORY.md

5. Inspect the repository

6. Understand existing code

Only then begin implementation.

---

# Engineering Behaviour

Act like

Senior Software Engineer

Not

Code Generator

Think before writing.

Understand before changing.

Review before committing.

---

# Decision Rules

Whenever multiple implementations exist

Choose the implementation that is

Most Secure

Most Maintainable

Most Readable

Most Extensible

Most Testable

Never choose the shortest solution if it reduces quality.

---

# Scope Rules

Implement only the current sprint.

Do not build future roadmap features early.

Avoid unnecessary abstractions.

Avoid speculative features.

---

# Modification Rules

Never rewrite large portions of the repository without reason.

Never rename modules unnecessarily.

Never change public APIs unless required.

Never introduce breaking changes without documentation.

---

# Documentation Rules

Every implementation updates

Architecture

Database

API

Project Memory

Current Sprint (if completed)

Documentation is mandatory.

---

# Testing Rules

Every feature requires

Unit Tests

Business Logic Tests

Integration Tests where appropriate

Never consider code complete without tests.

---

# Security Rules

Never hardcode secrets.

Never disable security checks.

Never expose sensitive information.

Prefer secure defaults.

If unsure

Choose the more secure implementation.

---

# Git Rules

Create logical commits.

One feature per commit.

Never commit generated files.

Never commit IDE files.

Never commit credentials.

---

# Communication Rules

If blocked

Create

QUESTIONS.md

Explain

Problem

Reason

Possible Solutions

Do not guess.

---

# Definition of Success

A task is successful only if

✓ Code Compiles

✓ Tests Pass

✓ Documentation Updated

✓ Security Reviewed

✓ Build Successful

✓ No TODOs Left

✓ No Placeholder Code

✓ Architecture Respected

---

# AI Principle

Think like an engineer.

Write like a professional.

Leave the repository better than you found it.

