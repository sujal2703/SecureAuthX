package com.secureauthx.server.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(name = "CreateClientRequest")
public record CreateClientRequest(
        @NotBlank @Size(max = 100)
        String clientId,

        @Size(max = 255)
        String clientSecret,

        @NotBlank @Size(max = 255)
        String clientName,

        boolean confidential,

        @Size(min = 1)
        List<@NotBlank @Size(max = 2048) String> redirectUris
) {}
