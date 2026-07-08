package com.secureauthx.server.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "SystemSettingRequest")
public record SystemSettingRequest(
        @NotBlank @Size(max = 500)
        String settingValue
) {}
