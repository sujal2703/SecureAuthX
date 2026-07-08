package com.secureauthx.server.developer.dto;

import com.secureauthx.server.developer.entity.DeveloperProject;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "ProjectResponse")
public record ProjectResponse(
        UUID id,
        UUID userId,
        String name,
        String description,
        UUID oauthClientId,
        boolean enabled,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProjectResponse from(DeveloperProject project) {
        return new ProjectResponse(
                project.getId(),
                project.getUserId(),
                project.getName(),
                project.getDescription(),
                project.getOauthClientId(),
                project.isEnabled(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
