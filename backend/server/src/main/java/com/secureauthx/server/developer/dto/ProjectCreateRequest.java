package com.secureauthx.server.developer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(name = "ProjectCreateRequest")
public record ProjectCreateRequest(
        @NotBlank @Size(max = 255)
        String name,

        @Size(max = 4000)
        String description,

        UUID oauthClientId
) {}
