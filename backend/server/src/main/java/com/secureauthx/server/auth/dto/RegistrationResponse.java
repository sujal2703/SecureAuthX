package com.secureauthx.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "RegistrationResponse")
public record RegistrationResponse(
        UUID id,
        String email,
        OffsetDateTime createdAt
) {
}
