package com.secureauthx.server.passkey.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthenticateVerificationResponse")
public record AuthenticateVerificationResponse(
        boolean verified,
        String credentialId,
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType
) {
    public static AuthenticateVerificationResponse verified(String credentialId, String accessToken,
                                                            String refreshToken, long expiresIn) {
        return new AuthenticateVerificationResponse(true, credentialId, accessToken, refreshToken, expiresIn, "Bearer");
    }

    public static AuthenticateVerificationResponse failed(String credentialId) {
        return new AuthenticateVerificationResponse(false, credentialId, null, null, 0, null);
    }
}
