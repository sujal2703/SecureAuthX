# SecureAuthX Deployment Guide

## Overview

SecureAuthX is deployed as a Docker Compose stack with three services:

- **postgres** (PostgreSQL 17)
- **redis** (Redis 7)
- **backend** (Spring Boot application)

---

## Prerequisites

- Docker 24+
- Docker Compose v2+
- Git

---

## Local Deployment

```bash
# Clone the repository
git clone https://github.com/anomalyco/SecureAuthX.git
cd SecureAuthX

# Configure environment
cp .env.example .env
# Edit .env with secure passwords

# Build and start
docker compose --env-file .env up --build -d

# Verify
curl http://localhost:8080/actuator/health
```

The stack starts in this order:
1. PostgreSQL (health check: pg_isready)
2. Redis (health check: redis-cli ping)
3. Backend (health check: /actuator/health/liveness)

---

## Production Deployment

### Environment Variables

All production secrets must be provided via environment variables. Required in production:

```
SECUREAUTHX_PROFILE=prod
SECUREAUTHX_POSTGRES_PASSWORD=<strong-password>
SECUREAUTHX_REDIS_PASSWORD=<strong-password>
SECUREAUTHX_JWT_PRIVATE_KEY=<base64-pkcs8-private-key>
SECUREAUTHX_JWT_PUBLIC_KEY=<base64-x509-public-key>
SECUREAUTHX_RATE_LIMITING_ENABLED=true
SECUREAUTHX_CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

The application validates that all required production variables are set at startup and fails immediately if any are missing.

### JWT Keys

In production, persistent RSA keys are required:

```bash
# Generate key pair
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -pubout -in private.pem -out public.pem

# Convert to base64
base64 -w0 private.pem > private-key.txt
base64 -w0 public.pem > public-key.txt
```

Set `SECUREAUTHX_JWT_PRIVATE_KEY` and `SECUREAUTHX_JWT_PUBLIC_KEY` to the base64-encoded values.

### Docker Compose (Production)

Create a production `docker-compose.prod.yml` or use environment file overrides:

```yaml
version: '3.8'
services:
  backend:
    environment:
      SECUREAUTHX_PROFILE: prod
      SECUREAUTHX_RATE_LIMITING_ENABLED: "true"
```

### Database

- PostgreSQL data persists in a Docker volume (`postgres-data`)
- Regular backups should be configured
- Connection pooling is configured via HikariCP (max 10 connections by default)
- Flyway runs migrations automatically on startup

---

## Scaling

The backend is stateless (sessions are stored in the database, not in memory). To scale horizontally:

1. Ensure all instances share the same PostgreSQL and Redis
2. Use persistent JWT keys across all instances
3. Place behind a load balancer (e.g., Nginx, HAProxy)
4. Configure `SECUREAUTHX_CORS_ALLOWED_ORIGINS` with the load balancer URL

---

## Health Checks

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health` | Overall health (includes db + redis) |
| `/actuator/health/liveness` | Liveness probe (is the app running?) |
| `/actuator/health/readiness` | Readiness probe (can it serve traffic?) |
| `/actuator/info` | Build info |
| `/actuator/metrics` | Micrometer metrics |
| `/actuator/prometheus` | Prometheus scrape endpoint |

---

## Monitoring

- Prometheus metrics at `/actuator/prometheus`
- Metrics include: JVM, database pool, HTTP requests (latency, count, errors)
- Health probes support container orchestration (Kubernetes, Docker Swarm)

---

## Security

- HTTPS must be configured at the reverse proxy/load balancer level
- HSTS is configured for 1 year with includeSubDomains
- CSP restricts resources to 'self' only
- CORS is configured via `SECUREAUTHX_CORS_ALLOWED_ORIGINS` — no wildcard in production
- Rate limiting is Redis-backed and configurable
- Default admin credentials must be changed immediately
