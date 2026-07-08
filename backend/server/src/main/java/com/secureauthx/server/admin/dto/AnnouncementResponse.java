package com.secureauthx.server.admin.dto;

import com.secureauthx.server.admin.entity.SystemAnnouncement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "AnnouncementResponse")
public record AnnouncementResponse(
        UUID id,
        String title,
        String message,
        String severity,
        boolean active,
        UUID createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static AnnouncementResponse from(SystemAnnouncement announcement) {
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getMessage(),
                announcement.getSeverity(),
                announcement.isActive(),
                announcement.getCreatedBy(),
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );
    }
}
