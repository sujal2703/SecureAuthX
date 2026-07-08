# 🔐 SecureAuthX

> **A production-ready Identity & Access Management (IAM) platform built with Spring Boot.**

SecureAuthX is a backend platform that implements modern authentication, authorization, identity federation, passkey authentication, developer tooling, and production-ready deployment practices.

It was built as an end-to-end engineering project to demonstrate enterprise authentication concepts including OAuth 2.1, OpenID Connect, WebAuthn (Passkeys), RBAC, multi-tenancy, adaptive risk assessment, and production operations.

---

## ✨ Features

### Authentication

- User Registration
- Secure Login
- JWT Authentication (RS256)
- Refresh Token Rotation
- Session Management
- Device Management
- Passkey (WebAuthn/FIDO2) Authentication

### Authorization

- Role-Based Access Control (RBAC)
- Organizations
- Multi-Tenant Architecture

### Identity Federation

- OAuth 2.1 Authorization Server
- OpenID Connect Provider
- PKCE (S256)

### Security

- Adaptive Risk Assessment
- Redis-backed Rate Limiting
- Security Headers
- Structured Logging
- Audit Logging
- Security Incident Tracking

### Developer Platform

- OAuth Client Management
- API Key Management
- Secret Rotation
- Usage Analytics
- Rate Limit Configuration

### Administration

- Admin Dashboard APIs
- System Settings
- Audit Log APIs
- Security Incident APIs
- Announcements

### Production Readiness

- Docker Support
- PostgreSQL
- Redis
- Flyway Database Migrations
- GitHub Actions CI
- Spring Boot Actuator
- Micrometer
- Prometheus Metrics

---

# 🏗 Architecture

SecureAuthX is organized into modular components:

```
                    SecureAuthX

                           │

        ┌──────────────────┼──────────────────┐

        │                  │                  │

 Authentication       Authorization      Platform

        │                  │                  │

 Login             OAuth 2.1          Developer APIs

 Sessions          OpenID Connect     Admin APIs

 Passkeys          RBAC               Audit Logs

 Risk Engine       Organizations      Analytics
```

---

# 🛠 Technology Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot |
| Security | Spring Security |
| Database | PostgreSQL 17 |
| Cache | Redis 7 |
| ORM | Spring Data JPA |
| Database Migration | Flyway |
| Authentication | JWT (RS256), OAuth 2.1, OpenID Connect, WebAuthn |
| Build Tool | Gradle |
| Containerization | Docker & Docker Compose |
| API Documentation | Swagger / OpenAPI |
| Monitoring | Spring Boot Actuator, Micrometer, Prometheus |
| CI/CD | GitHub Actions |

---

# 📂 Project Structure

```
SecureAuthX
│
├── backend/
│   └── server/
│
├── frontend/
│
├── docs/
│
├── infrastructure/
│
├── scripts/
│
├── .github/
│
├── docker-compose.yml
│
└── README.md
```

---

# 🚀 Quick Start

## Clone Repository

```bash
git clone https://github.com/sujal2703/SecureAuthX.git
cd SecureAuthX
```

## Configure Environment

```bash
cp .env.example .env
```

Update the values inside `.env` before starting the application.

## Start Services

```bash
docker compose up --build
```

---

# 📖 API Documentation

Swagger UI

```
http://localhost:8080/swagger-ui
```

OpenAPI Specification

```
http://localhost:8080/v3/api-docs
```

Health Endpoint

```
http://localhost:8080/actuator/health
```

---

# 🧪 Running Tests

Run all tests using:

```bash
./gradlew build
```

---

# 📊 Project Status

| Item | Status |
|------|--------|
| Registration | ✅ |
| Authentication | ✅ |
| Session Management | ✅ |
| RBAC | ✅ |
| Organizations | ✅ |
| OAuth 2.1 | ✅ |
| OpenID Connect | ✅ |
| WebAuthn Passkeys | ✅ |
| Adaptive Risk Assessment | ✅ |
| Developer Portal | ✅ |
| Admin Portal | ✅ |
| Production Readiness | ✅ |

---

# 📋 Completed Roadmap

- Sprint 00 – Foundation
- Sprint 01 – Registration
- Sprint 02 – Authentication
- Sprint 03 – Session Management
- Sprint 04 – RBAC
- Sprint 05 – Organizations
- Sprint 06 – OAuth 2.1
- Sprint 07 – Passkeys (WebAuthn)
- Sprint 08 – OpenID Connect
- Sprint 09 – Adaptive Risk Assessment
- Sprint 10 – Developer Portal
- Sprint 11 – Admin Portal
- Sprint 12 – Production Readiness

---

# 🔮 Future Enhancements

Potential future work includes:

- Email Verification
- Password Reset
- Social Login Providers (Google, GitHub, etc.)
- TOTP-based Multi-Factor Authentication
- Frontend Dashboard
- Kubernetes Deployment
- Horizontal Scaling
- Advanced Threat Detection

---

# 📄 License

This project is licensed under the MIT License.

---

# 👨‍💻 Author

**Sujal Thumar**

SecureAuthX was built as a comprehensive portfolio project focused on modern Identity & Access Management concepts using Spring Boot and related technologies.