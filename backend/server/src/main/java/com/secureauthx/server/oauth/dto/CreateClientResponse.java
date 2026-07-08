package com.secureauthx.server.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(name = "CreateClientResponse")
public record CreateClientResponse(
        UUID id,
        String clientId,
        String clientSecret,
        String clientName,
        boolean confidential,
        boolean enabled,
        List<String> redirectUris,
        OffsetDateTime createdAt
) {}
