package com.secureauthx.server.developer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

@Schema(name = "CreateApiKeyRequest")
public record CreateApiKeyRequest(
        @NotBlank @Size(max = 255)
        String label,

        OffsetDateTime expiresAt
) {}
