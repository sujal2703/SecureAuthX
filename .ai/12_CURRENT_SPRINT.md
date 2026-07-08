# Sprint 12 — Production Readiness (Release Candidate)

Status: COMPLETED on 2026-07-08

## Objective

Prepare SecureAuthX for production deployment. Focus on stability, security, performance, observability, deployment, and CI/CD.

## Scope Implemented

### 1. Production Profiles
- `application.yml` — base config with `spring.profiles.active: ${SECUREAUTHX_PROFILE:local}`
- `application-dev.yml` — development with debug logging and detailed health
- `application-test.yml` — H2 in-memory, Redis disabled (pre-existing, updated)
- `application-prod.yml` — production tuning: HikariCP, Tomcat threads, connection timeouts, Prometheus metrics, percentile config, batch JDBC operations
- `ProductionConfig.java` — `@Profile("prod")` startup validation of required env vars (DB password, Redis password, JWT keys). Fails fast with meaningful messages.

### 2. Observability
- Added `micrometer-registry-prometheus` dependency
- Prometheus endpoint exposed at `/actuator/prometheus`
- Micrometer tags (`application=secureauthx`) on all metrics
- Latency percentiles (SLO) and histogram configured for http.server.requests
- Liveness and readiness probes enabled (pre-existing, verified working)
- Build info endpoint (`/actuator/info`) reports artifact version

### 3. Structured Logging
- `CorrelationFilter` generates unique request ID (`X-Request-ID`) for every request
- Request ID set as MDC value (`requestId`) and returned as response header
- Execution time measured and returned as `X-Execution-Time-Ms` response header
- `JwtAuthenticationFilter` updated to set `userId` and `sessionId` in MDC when JWT is validated
- Logback pattern updated to include `requestId`, `userId`, `sessionId` in every log line

### 4. Security Hardening
- `SecurityHeadersConfig` adds per-response headers:
  - `Strict-Transport-Security: max-age=31536000; includeSubDomains`
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `Referrer-Policy: strict-origin-when-cross-origin`
  - `Permissions-Policy: camera=(), microphone=(), geolocation=()`
  - `Cache-Control: no-store, max-age=0`
  - `Content-Security-Policy: default-src 'none'; ... frame-ancestors 'none'; form-action 'self'`
- `CorsConfig.java` — whitelist-based CORS via `SECUREAUTHX_CORS_ALLOWED_ORIGINS` env var
- SecurityConfig updated with `.cors(cors -> {})` to enable Spring CORS processing

### 5. Runtime Rate Limiting
- `RateLimitingService` — Redis-backed sliding window counter per user/IP/endpoint
- `RateLimitingFilter` — applies rate limits to `/api/v1/auth/login` (10/min), `/api/v1/auth/register` (10/min), `/api/v1/auth/refresh` (20/min)
- Returns HTTP 429 with JSON error body when exceeded
- `RateLimitConfig` — `@ConditionalOnProperty("secureauthx.rate-limiting.enabled")`, only active when explicitly enabled
- Configurable via `SECUREAUTHX_RATE_LIMITING_ENABLED`, `SECUREAUTHX_RATE_LIMIT_DEFAULT_RPM`, `SECUREAUTHX_RATE_LIMIT_DEFAULT_RPH`

### 6. Docker Improvements
- Healthcheck updated to use `/actuator/health/liveness` with `--start-period=60s`
- Production JVM options via `JAVA_OPTS` env: ZGC, MaxRAMPercentage=75%, ExitOnOutOfMemoryError, HeapDumpOnOOM
- Runtime image remains `eclipse-temurin:21-jre-jammy` with non-root user
- Multi-stage build preserved

### 7. GitHub Actions CI
- Created `.github/workflows/ci.yml`
- On push to main/develop and PR to main:
  - Build with Gradle
  - Run all 209 tests
  - Verify Docker build
  - Archive test reports on failure
- Uses PostgreSQL and Redis service containers

### 8. Performance
- HikariCP pool: max-lifetime (30min), leak-detection-threshold (60s) configured
- Tomcat: max-threads (200), min-spare (10), connection-timeout (5s), max-connections (10000), accept-count (100)
- Redis connection pooling (lettuce): max-active (16), max-idle (8), min-idle (2)
- Hibernate batch operations: batch_size (25), order_inserts/updates enabled
- Compression: mime-types extended, min-response-size (2KB)

### 9. Documentation
- `README.md` — created with project overview, features, architecture, tech stack, quick start, API overview, profiles, env vars, roadmap, license
- `docs/DEPLOYMENT.md` — created with local/prod deployment, JWT key generation, scaling, health checks, monitoring, security
- `docs/PRODUCTION.md` — created with pre/post-deployment checklists, architecture diagram, troubleshooting
- `IMPLEMENTATION_PLAN.md` — updated top section
- `.ai/12_CURRENT_SPRINT.md` — updated
- `.ai/13_PROJECT_MEMORY.md` — updated
- `.env.example` — updated with all new variables

## Architecture Decisions

### Rate Limiting as Conditional Feature
- Redis-backed rate limiting is disabled by default to avoid breaking existing tests
- Enabled via `SECUREAUTHX_RATE_LIMITING_ENABLED=true`
- `RateLimitConfig` uses `@ConditionalOnProperty` so the beans are only created when enabled
- `SecurityConfig` uses `ObjectProvider<RateLimitingFilter>` for optional injection

### Correlation Filter at Highest Precedence
- `CorrelationFilter` runs before the security filter chain to ensure MDC values are available for all downstream logging
- Request ID is generated server-side if not provided by the client

### Production Profile Validates at Startup
- `ProductionConfig` checks required env vars on `@PostConstruct`
- Application fails fast with descriptive message if any are missing
- Prevents deployment with default/incomplete configuration

## Files Created
- `backend/server/src/main/resources/application-dev.yml`
- `backend/server/src/main/resources/application-prod.yml`
- `backend/server/src/main/java/com/secureauthx/server/config/ProductionConfig.java`
- `backend/server/src/main/java/com/secureauthx/server/config/CorsConfig.java`
- `backend/server/src/main/java/com/secureauthx/server/config/SecurityHeadersConfig.java`
- `backend/server/src/main/java/com/secureauthx/server/config/CorrelationFilter.java`
- `backend/server/src/main/java/com/secureauthx/server/config/RateLimitingService.java`
- `backend/server/src/main/java/com/secureauthx/server/config/RateLimitingFilter.java`
- `backend/server/src/main/java/com/secureauthx/server/config/RateLimitConfig.java`
- `.github/workflows/ci.yml`
- `README.md`
- `docs/DEPLOYMENT.md`
- `docs/PRODUCTION.md`

## Files Modified
- `build.gradle.kts` — added micrometer-registry-prometheus
- `application.yml` — added profiles, prometheus, rate-limiting, cors config sections
- `application-test.yml` — unchanged (pre-existing)
- `logback-spring.xml` — updated pattern with requestId, userId, sessionId
- `Dockerfile` — updated HEALTHCHECK, JAVA_OPTS
- `.env.example` — added all new variables
- `docker-compose.yml` — unchanged
- `SecurityConfig.java` — added CORS, rate limiting filter, actuator endpoints
- `JwtAuthenticationFilter.java` — added MDC population
- `.ai/12_CURRENT_SPRINT.md` — updated
- `.ai/13_PROJECT_MEMORY.md` — updated

## Verification
- `./gradlew.bat build` succeeds (209 tests passing)
- `docker build -t secureauthx-server .` succeeds
- `docker compose up -d` starts all 3 services (postgres, redis, backend)
- `/actuator/health` returns `{"status":"UP","groups":["liveness","readiness"]}`
- `/actuator/health/liveness` returns `{"status":"UP"}`
- `/actuator/health/readiness` returns `{"status":"UP"}`
- `/actuator/info` returns build artifact info
- MDC values (requestId, userId, sessionId) appear in log output
- Security headers present in all responses
- CORS filter active with whitelist-based origins
- Rate limiting ready when enabled via environment variable
