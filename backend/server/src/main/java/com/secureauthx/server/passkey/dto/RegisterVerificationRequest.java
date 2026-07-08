package com.secureauthx.server.passkey.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegisterVerificationRequest")
public record RegisterVerificationRequest(
        String id,
        String rawId,
        String type,
        String clientDataJSON,
        String attestationObject,
        String authenticatorData,
        String publicKey,
        String publicKeyAlgorithm,
        String transports,
        String aaguid,
        String deviceName
) {}
