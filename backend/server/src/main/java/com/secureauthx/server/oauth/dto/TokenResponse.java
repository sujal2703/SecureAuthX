package com.secureauthx.server.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OAuthTokenResponse")
public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        String scope,
        String idToken
) {
    public TokenResponse(String accessToken, long expiresIn) {
        this(accessToken, "Bearer", expiresIn, null, null, null);
    }

    public TokenResponse(String accessToken, long expiresIn, String refreshToken) {
        this(accessToken, "Bearer", expiresIn, refreshToken, null, null);
    }

    public TokenResponse(String accessToken, long expiresIn, String refreshToken, String scope, String idToken) {
        this(accessToken, "Bearer", expiresIn, refreshToken, scope, idToken);
    }
}
