package com.secureauthx.server.developer.dto;

import com.secureauthx.server.developer.entity.DeveloperApiKey;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "ApiKeyResponse")
public record ApiKeyResponse(
        UUID id,
        UUID projectId,
        String keyPrefix,
        String label,
        OffsetDateTime lastUsedAt,
        OffsetDateTime expiresAt,
        boolean enabled,
        OffsetDateTime createdAt
) {
    public static ApiKeyResponse from(DeveloperApiKey key) {
        return new ApiKeyResponse(
                key.getId(),
                key.getProjectId(),
                key.getKeyPrefix(),
                key.getLabel(),
                key.getLastUsedAt(),
                key.getExpiresAt(),
                key.isEnabled(),
                key.getCreatedAt()
        );
    }
}
