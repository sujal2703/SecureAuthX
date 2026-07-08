package com.secureauthx.server.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AnnouncementRequest")
public record AnnouncementRequest(
        @NotBlank @Size(max = 255)
        String title,

        @NotBlank
        String message,

        @Size(max = 20)
        String severity,

        Boolean active
) {}
