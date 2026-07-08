package com.secureauthx.server.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class ProductionConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductionConfig.class);

    @PostConstruct
    void validateProductionConfiguration() {
        LOGGER.info("Validating production configuration...");

        checkEnv("SECUREAUTHX_POSTGRES_PASSWORD", "PostgreSQL password");
        checkEnv("SECUREAUTHX_REDIS_PASSWORD", "Redis password");
        checkEnv("SECUREAUTHX_JWT_PRIVATE_KEY", "JWT private key");
        checkEnv("SECUREAUTHX_JWT_PUBLIC_KEY", "JWT public key");

        String dbUrl = System.getenv("SECUREAUTHX_POSTGRES_DB");
        if (dbUrl == null || dbUrl.isBlank()) {
            fail("SECUREAUTHX_POSTGRES_DB is required in production");
        }

        LOGGER.info("Production configuration validation complete");
    }

    private void checkEnv(String name, String description) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            fail(description + " (environment variable " + name + ") is required in production");
        }
    }

    private void fail(String message) {
        LOGGER.error("Production configuration error: {}", message);
        throw new IllegalStateException("Production configuration error: " + message);
    }
}
