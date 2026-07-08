package com.secureauthx.server.passkey.dto;

import com.secureauthx.server.passkey.entity.Passkey;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

@Schema(name = "PasskeyResponse")
public record PasskeyResponse(
        UUID id,
        String credentialId,
        String deviceName,
        String aaguid,
        String credentialType,
        boolean backedUp,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static PasskeyResponse from(Passkey passkey) {
        return new PasskeyResponse(
                passkey.getId(),
                Base64.getUrlEncoder().withoutPadding().encodeToString(
                        passkey.getCredentialId().getBytes(java.nio.charset.StandardCharsets.UTF_8)
                ),
                passkey.getDeviceName(),
                passkey.getAaguid(),
                passkey.getCredentialType(),
                passkey.isBackedUp(),
                passkey.getCreatedAt(),
                passkey.getUpdatedAt()
        );
    }
}
