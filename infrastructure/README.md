# 🔐 SecureAuthX

> **Production-ready Identity & Access Management (IAM) platform built with Spring Boot.**


![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791)
![Redis](https://img.shields.io/badge/Redis-7-DC382D)
![OAuth 2.1](https://img.shields.io/badge/OAuth-2.1-success)
![OpenID Connect](https://img.shields.io/badge/OpenID_Connect-1.0-success)
![WebAuthn](https://img.shields.io/badge/WebAuthn-Passkeys-success)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)
![GitHub Actions](https://img.shields.io/badge/CI-GitHub_Actions-success)
![License](https://img.shields.io/badge/License-MIT-blue)

</p>

---

# 📖 Overview

SecureAuthX is a **production-ready Identity & Access Management (IAM) platform** designed to demonstrate modern authentication, authorization, identity federation, and secure backend engineering using the Spring ecosystem.

The platform was built incrementally through **12 implementation sprints**, following enterprise software development practices. It combines modern authentication standards such as **OAuth 2.1**, **OpenID Connect 1.0**, **JWT (RS256)**, and **WebAuthn Passkeys** with production-grade infrastructure including Docker, PostgreSQL, Redis, Flyway migrations, structured logging, monitoring, GitHub Actions CI, and secure deployment practices.

Rather than being a simple authentication demo, SecureAuthX aims to model how a modern Identity Provider (IdP) can be designed using modular architecture, secure coding practices, and industry standards.

---

# ✨ Core Features

## 🔑 Authentication

- User Registration
- Secure Login
- JWT Authentication (RS256)
- Refresh Token Rotation
- Session Management
- Device Tracking
- WebAuthn / FIDO2 Passkeys
- Secure Logout

---

## 🛡 Authorization

- Role-Based Access Control (RBAC)
- Permission-Based Authorization
- Organizations
- Multi-Tenant Architecture
- Organization Membership

---

## 🌐 Identity Federation

- OAuth 2.1 Authorization Server
- OpenID Connect Provider
- Authorization Code Flow
- PKCE (S256)
- Client Credentials Flow
- Discovery Endpoint
- JWKS Endpoint
- UserInfo Endpoint

---

## 🔒 Security

- Adaptive Risk Assessment
- Redis-backed Rate Limiting
- Security Headers
- Content Security Policy
- Structured Logging
- Correlation IDs
- Audit Logging
- Security Incident Tracking
- Refresh Token Rotation
- Session Revocation

---

## 👨‍💻 Developer Platform

- OAuth Client Registration
- API Key Management
- Secret Rotation
- Usage Analytics
- Rate Limit Configuration
- Developer Projects

---

## ⚙️ Administration

- Dashboard APIs
- Audit Logs
- Security Incidents
- System Announcements
- Runtime Settings
- Platform Monitoring

---

## 🚀 Production Readiness

- Docker
- Docker Compose
- PostgreSQL 17
- Redis 7
- Flyway Database Migrations
- Spring Boot Actuator
- Micrometer Metrics
- Prometheus Integration
- GitHub Actions CI
- Production Profiles
- Structured Logging
- Health Checks
- Graceful Shutdown

---

# 🏗 System Architecture

SecureAuthX follows a modular architecture where authentication, authorization, identity federation, developer tooling, administration, and production infrastructure are implemented as independent modules.

The complete architecture documentation is available inside **`docs/diagrams/`**.

### Architecture Diagrams

- 📌 System Architecture
- 📌 Authentication Flow
- 📌 OAuth 2.1 Authorization Code + PKCE Flow
- 📌 WebAuthn Passkey Authentication Flow
- 📌 Database Entity Relationship Diagram

---

# 🧩 Platform Modules

| Module | Purpose |
|---------|---------|
| Authentication | Registration, Login, JWT, Refresh Tokens |
| Session Management | Device tracking, session revocation |
| RBAC | Roles, permissions and authorization |
| Organizations | Multi-tenant workspace management |
| OAuth 2.1 | Authorization Server implementation |
| OpenID Connect | Identity Provider implementation |
| WebAuthn | Passwordless authentication using Passkeys |
| Adaptive Risk | Login risk evaluation |
| Developer Portal | OAuth client & API key management |
| Admin Portal | Platform administration |
| Production | Monitoring, logging, deployment & security |

---

# 🛠 Technology Stack

| Category | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Security | Spring Security |
| Authentication | JWT (RS256), OAuth 2.1, OpenID Connect 1.0, WebAuthn |
| Database | PostgreSQL 17 |
| Cache | Redis 7 |
| ORM | Spring Data JPA / Hibernate |
| Database Migration | Flyway |
| Build Tool | Gradle |
| Containerization | Docker & Docker Compose |
| API Documentation | OpenAPI 3 / Swagger UI |
| Monitoring | Spring Boot Actuator, Micrometer, Prometheus |
| Logging | Logback + MDC Correlation IDs |
| CI/CD | GitHub Actions |

# 📂 Project Structure

```text
SecureAuthX
│
├── backend/
│   └── server/                 # Spring Boot application
│
├── docs/                       # Technical documentation
│   ├── diagrams/
│   ├── API_SPEC.md
│   ├── DATABASE.md
│   ├── SYSTEM_DESIGN.md
│   ├── USER_FLOWS.md
│   ├── DEPLOYMENT.md
│   └── PRODUCTION.md
│
├── infrastructure/             # Infrastructure guides
│
├── .github/
│   └── workflows/
│
├── docker-compose.yml
├── README.md
└── LICENSE
```

---

# 🚀 Quick Start

## 1. Clone the Repository

```bash
git clone https://github.com/sujal2703/SecureAuthX.git

cd SecureAuthX
```

---

## 2. Configure Environment

Copy the example environment file.

```bash
cp .env.example .env
```

Update all required environment variables before starting the application.

---

## 3. Start the Platform

```bash
docker compose up --build
```

This starts:

- Spring Boot Backend
- PostgreSQL 17
- Redis 7

---

## 4. Verify Installation

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

Prometheus Metrics

```
http://localhost:8080/actuator/prometheus
```

---

# 🐳 Docker

SecureAuthX is fully containerized.

Start the complete development environment using:

```bash
docker compose up --build
```

Stop the environment:

```bash
docker compose down
```

---

# 📖 API Documentation

The backend automatically generates OpenAPI documentation.

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |
| Metrics | http://localhost:8080/actuator/prometheus |

---

# 🧪 Running Tests

Run all automated tests.

```bash
./gradlew build
```

Or execute only the test suite.

```bash
./gradlew test
```

Current status:

- ✅ 209 Automated Tests
- ✅ Unit Tests
- ✅ Integration Tests
- ✅ OAuth Tests
- ✅ OIDC Tests
- ✅ Passkey Tests
- ✅ GitHub Actions CI Passing

---

# 📊 Project Statistics

| Metric | Value |
|----------|---------|
| Development Sprints | 12 |
| Automated Tests | 209 |
| OAuth Version | OAuth 2.1 |
| OpenID Connect | 1.0 |
| Authentication Methods | JWT + Passkeys |
| Database | PostgreSQL 17 |
| Cache | Redis 7 |
| Build Tool | Gradle |
| CI/CD | GitHub Actions |
| Production Profiles | Local / Dev / Test / Prod |

---

# ✅ Project Status

| Module | Status |
|---------|--------|
| Foundation | ✅ Complete |
| Registration | ✅ Complete |
| Authentication | ✅ Complete |
| Session Management | ✅ Complete |
| RBAC | ✅ Complete |
| Organizations | ✅ Complete |
| OAuth 2.1 | ✅ Complete |
| WebAuthn Passkeys | ✅ Complete |
| OpenID Connect | ✅ Complete |
| Adaptive Risk Assessment | ✅ Complete |
| Developer Portal | ✅ Complete |
| Admin Portal | ✅ Complete |
| Production Readiness | ✅ Complete |

---

# 🗺 Development Timeline

| Sprint | Feature |
|---------|---------|
| Sprint 00 | Project Foundation |
| Sprint 01 | User Registration |
| Sprint 02 | Authentication |
| Sprint 03 | Session Management |
| Sprint 04 | RBAC |
| Sprint 05 | Organizations |
| Sprint 06 | OAuth 2.1 Authorization Server |
| Sprint 07 | WebAuthn / Passkeys |
| Sprint 08 | OpenID Connect |
| Sprint 09 | Adaptive Risk Assessment |
| Sprint 10 | Developer Portal |
| Sprint 11 | Admin Portal |
| Sprint 12 | Production Readiness |

---

# 📚 Documentation

Additional documentation is available inside the **docs/** directory.

- API Specification
- Database Design
- System Design
- User Flows
- Deployment Guide
- Production Guide
- Architecture Diagrams
- Sprint Documentation

# 🔮 Future Enhancements

Although SecureAuthX is feature complete as a backend IAM platform, several enhancements are planned for future releases.

## Identity & Authentication

- Email Verification
- Password Reset
- Social Login (Google, GitHub, Microsoft)
- TOTP-based Multi-Factor Authentication (MFA)
- Adaptive Authentication Policies
- Device Trust Management

---

## Platform

- Frontend Dashboard (Next.js / React)
- User Self-Service Portal
- Organization Management UI
- Admin Dashboard UI
- Developer Console UI

---

## Infrastructure

- Kubernetes Deployment
- Horizontal Scaling
- Distributed Session Clustering
- High Availability PostgreSQL
- Automated Backup & Recovery
- Infrastructure as Code (Terraform)

---

## Security

- Hardware Security Module (HSM) Support
- Automatic Key Rotation
- SIEM Integration
- Threat Intelligence Integration
- Security Compliance Reports
- Advanced Risk Detection

---

# 🤝 Contributing

Contributions, bug reports, feature requests, and suggestions are welcome.

If you would like to contribute:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a Pull Request

Please ensure that new features include appropriate tests and documentation.

---

# 📄 License

This project is licensed under the **MIT License**.

See the **LICENSE** file for details.

---

# 👨‍💻 Author

## Sujal Thumar

SecureAuthX was designed and implemented as an end-to-end **Identity & Access Management (IAM)** platform to explore modern authentication standards, secure backend engineering, and production-ready software architecture.

The project demonstrates practical implementation of:

- OAuth 2.1 Authorization Server
- OpenID Connect Provider
- WebAuthn / FIDO2 Passkeys
- JWT Authentication
- Role-Based Access Control (RBAC)
- Organizations & Multi-Tenancy
- Adaptive Risk Assessment
- Developer Portal
- Admin Portal
- Production Deployment Practices

The goal of SecureAuthX is to serve as both a learning project and a production-inspired backend architecture demonstrating enterprise authentication and authorization concepts using the Spring ecosystem.

---

# ⭐ Acknowledgements

SecureAuthX is inspired by modern Identity Providers and authentication platforms, including industry standards and best practices around:

- OAuth 2.1
- OpenID Connect 1.0
- WebAuthn / FIDO2
- Spring Security
- OWASP Security Guidelines
- RFC-based Authentication Standards

---



### 🔐 SecureAuthX

**Secure Authentication. Modern Identity. Production-Ready Architecture.**

Built with ❤️ using **Java 21**, **Spring Boot**, **PostgreSQL**, **Redis**, **Docker**, and modern Identity standards.

