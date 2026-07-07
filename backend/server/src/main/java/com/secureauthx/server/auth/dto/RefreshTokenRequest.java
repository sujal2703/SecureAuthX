package com.secureauthx.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "RefreshTokenRequest")
public record RefreshTokenRequest(
        @Schema(example = "a1b2c3d4e5f6...")
        @NotBlank(message = "Refresh token is required.")
        String refreshToken
) {
}
