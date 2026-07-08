package com.secureauthx.server.config;

import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RateLimitingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingService.class);

    private final StringRedisTemplate redisTemplate;

    public RateLimitingService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(UUID userId, int maxRequests, Duration window) {
        return isAllowed("user:" + userId.toString(), maxRequests, window);
    }

    public boolean isAllowedByIp(String ipAddress, int maxRequests, Duration window) {
        return isAllowed("ip:" + ipAddress, maxRequests, window);
    }

    boolean isAllowed(String key, int maxRequests, Duration window) {
        String redisKey = "ratelimit:" + key;
        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count != null && count == 1) {
                redisTemplate.expire(redisKey, window);
            }
            return count != null && count <= maxRequests;
        } catch (Exception e) {
            LOGGER.warn("Rate limiting check failed, allowing request: {}", e.getMessage());
            return true;
        }
    }
}
