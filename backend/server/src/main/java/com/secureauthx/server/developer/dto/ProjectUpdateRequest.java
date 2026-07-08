package com.secureauthx.server.developer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "ProjectUpdateRequest")
public record ProjectUpdateRequest(
        @Size(max = 255)
        String name,

        @Size(max = 4000)
        String description
) {}
