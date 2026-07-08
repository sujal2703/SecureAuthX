# SecureAuthX Production Checklist

## Pre-Deployment Checklist

### Required Configuration

- [ ] `SECUREAUTHX_PROFILE` set to `prod`
- [ ] `SECUREAUTHX_POSTGRES_PASSWORD` is a strong, unique password
- [ ] `SECUREAUTHX_REDIS_PASSWORD` is a strong, unique password
- [ ] `SECUREAUTHX_JWT_PRIVATE_KEY` set to a persistent RSA private key (base64 PKCS8)
- [ ] `SECUREAUTHX_JWT_PUBLIC_KEY` set to the corresponding public key (base64 X.509)
- [ ] `SECUREAUTHX_CORS_ALLOWED_ORIGINS` set to specific production domains (no wildcard)
- [ ] `SECUREAUTHX_RATE_LIMITING_ENABLED` set to `true`

### Database

- [ ] PostgreSQL 17 is running with persistent storage
- [ ] Automated backups are configured
- [ ] Backup restore process has been tested
- [ ] Connection pooling limits are configured appropriately
- [ ] Flyway migrations have been applied and verified

### Redis

- [ ] Redis 7 is running with password authentication
- [ ] Redis persistence (AOF/RDB) is configured
- [ ] Rate limiting data can be lost on restart (acceptable)

### Security

- [ ] HTTPS is enforced at the reverse proxy or load balancer
- [ ] HSTS is configured (included in application headers: max-age=31536000)
- [ ] CSP headers are configured and tested
- [ ] CORS origins are whitelisted (no wildcard)
- [ ] Admin accounts are created with strong passwords
- [ ] Default test accounts are removed
- [ ] Database credentials are rotated from defaults

### Monitoring

- [ ] Health endpoints are accessible from monitoring system
- [ ] Prometheus scraping is configured
- [ ] JVM metrics are being collected
- [ ] Request latency metrics are being monitored
- [ ] Error rates are being monitored
- [ ] Alerts are configured for service degradation

### Logging

- [ ] Log levels are set to INFO (not DEBUG)
- [ ] Log aggregation is configured (e.g., ELK, Datadog, Grafana Loki)
- [ ] No sensitive data is being logged
- [ ] Log retention policy is configured

### Performance

- [ ] HikariCP pool size is tuned for expected load
- [ ] Tomcat thread pool is tuned
- [ ] Connection timeouts are configured
- [ ] JVM heap and GC settings are appropriate for the workload

### Application

- [ ] All 209 tests pass
- [ ] Build succeeds
- [ ] Docker image builds successfully
- [ ] Health endpoint returns UP
- [ ] Liveness and readiness probes respond correctly
- [ ] JWT keys are persistent (not ephemeral)

---

## Post-Deployment Verification

```bash
# Health check
curl https://yourdomain.com/actuator/health

# Liveness probe
curl https://yourdomain.com/actuator/health/liveness

# Readiness probe
curl https://yourdomain.com/actuator/health/readiness

# Build info
curl https://yourdomain.com/actuator/info

# Verify registration works
curl -X POST https://yourdomain.com/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"TestPass123!"}'

# Verify login works
curl -X POST https://yourdomain.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"TestPass123!"}'
```

---

## Production Architecture

```
                     ┌─────────────┐
                     │   Client     │
                     └──────┬──────┘
                            │ HTTPS
                     ┌──────▼──────┐
                     │  Load       │
                     │  Balancer   │
                     │ (Nginx/HA)  │
                     └──────┬──────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
     ┌────────▼───┐  ┌─────▼──────┐  ┌───▼────────┐
     │  Backend   │  │  Backend   │  │  Backend   │
     │  Instance  │  │  Instance  │  │  Instance  │
     └────────┬───┘  └─────┬──────┘  └───┬────────┘
              │             │             │
              └─────────────┼─────────────┘
                            │
              ┌─────────────▼─────────────┐
              │     PostgreSQL 17         │
              └─────────────┬─────────────┘
                            │
              ┌─────────────▼─────────────┐
              │        Redis 7            │
              └───────────────────────────┘
```

---

## Troubleshooting

### Application fails to start

Check logs:
```bash
docker compose logs backend
```

Verify environment variables are set correctly. In production mode, the application validates required variables and fails fast if any are missing.

### Health check fails

- Ensure PostgreSQL and Redis are running and healthy
- Check database connection settings
- Verify network connectivity between containers

### Slow response times

- Check HikariCP pool usage in metrics (`/actuator/metrics/hikaricp.connections.active`)
- Check Tomcat thread usage (`/actuator/metrics/tomcat.threads.busy`)
- Review database query performance
- Consider scaling horizontally with additional instances

### Rate limiting issues

- Verify Redis is running and accessible
- Check `SECUREAUTHX_RATE_LIMITING_ENABLED` is set to `true`
- Rate limit counters reset after the configured window (1 minute for auth, 1 hour for general)
