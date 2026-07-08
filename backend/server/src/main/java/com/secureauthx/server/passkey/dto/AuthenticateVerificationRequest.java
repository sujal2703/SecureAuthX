package com.secureauthx.server.passkey.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthenticateVerificationRequest")
public record AuthenticateVerificationRequest(
        String id,
        String rawId,
        String type,
        String clientDataJSON,
        String authenticatorData,
        String signature,
        String userHandle
) {}
