package com.secureauthx.server.passkey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "RegisterVerificationResponse")
public record RegisterVerificationResponse(
        UUID id,
        boolean verified,
        String credentialId
) {}
