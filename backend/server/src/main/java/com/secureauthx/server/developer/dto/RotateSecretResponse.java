package com.secureauthx.server.developer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "RotateSecretResponse")
public record RotateSecretResponse(
        UUID projectId,
        UUID oauthClientId,
        String newClientSecret,
        OffsetDateTime rotatedAt
) {}
