package com.secureauthx.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnProperty(name = "secureauthx.rate-limiting.enabled", havingValue = "true")
public class RateLimitConfig {

    @Bean
    RateLimitingService rateLimitingService(StringRedisTemplate redisTemplate) {
        return new RateLimitingService(redisTemplate);
    }

    @Bean
    RateLimitingFilter rateLimitingFilter(RateLimitingService rateLimitingService) {
        return new RateLimitingFilter(rateLimitingService);
    }
}
