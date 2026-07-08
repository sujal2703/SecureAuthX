package com.secureauthx.server.admin.dto;

import com.secureauthx.server.admin.entity.SystemSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "SystemSettingResponse")
public record SystemSettingResponse(
        UUID id,
        String settingKey,
        String settingValue,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static SystemSettingResponse from(SystemSetting setting) {
        return new SystemSettingResponse(
                setting.getId(),
                setting.getSettingKey(),
                setting.getSettingValue(),
                setting.getDescription(),
                setting.getCreatedAt(),
                setting.getUpdatedAt()
        );
    }
}
