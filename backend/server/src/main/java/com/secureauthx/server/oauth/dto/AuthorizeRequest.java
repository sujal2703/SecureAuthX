package com.secureauthx.server.oauth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthorizeRequest(
        @NotBlank String clientId,
        @NotBlank String redirectUri,
        @NotBlank String responseType,
        String scope,
        @NotBlank String state,
        @NotBlank String codeChallenge,
        @NotBlank String codeChallengeMethod
) {}
