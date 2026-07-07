package com.secureauthx.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "LoginRequest")
public record LoginRequest(
        @Schema(example = "developer@example.com")
        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        @Size(max = 320, message = "Email must be 320 characters or fewer.")
        String email,

        @Schema(example = "S3cureExample!2026")
        @NotBlank(message = "Password is required.")
        String password
) {
}
