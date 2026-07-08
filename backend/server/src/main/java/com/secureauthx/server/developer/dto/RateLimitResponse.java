package com.secureauthx.server.developer.dto;

import com.secureauthx.server.developer.entity.ApiRateLimit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "RateLimitResponse")
public record RateLimitResponse(
        UUID id,
        UUID projectId,
        int requestsPerMinute,
        int requestsPerHour,
        boolean enabled,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static RateLimitResponse from(ApiRateLimit rateLimit) {
        return new RateLimitResponse(
                rateLimit.getId(),
                rateLimit.getProjectId(),
                rateLimit.getRequestsPerMinute(),
                rateLimit.getRequestsPerHour(),
                rateLimit.isEnabled(),
                rateLimit.getCreatedAt(),
                rateLimit.getUpdatedAt()
        );
    }
}
