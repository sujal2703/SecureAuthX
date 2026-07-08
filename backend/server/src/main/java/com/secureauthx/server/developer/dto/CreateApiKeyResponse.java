package com.secureauthx.server.developer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "CreateApiKeyResponse")
public record CreateApiKeyResponse(
        UUID id,
        UUID projectId,
        String keyPrefix,
        String plainTextKey,
        String label,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt
) {}
