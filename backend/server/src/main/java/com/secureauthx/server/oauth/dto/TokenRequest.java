package com.secureauthx.server.oauth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank String grantType,
        String code,
        String redirectUri,
        String clientId,
        String clientSecret,
        String codeVerifier,
        String scope
) {}
