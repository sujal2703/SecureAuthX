package com.secureauthx.server.admin.dto;

import com.secureauthx.server.admin.entity.SecurityIncident;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "SecurityIncidentResponse")
public record SecurityIncidentResponse(
        UUID id,
        UUID userId,
        String incidentType,
        String severity,
        String description,
        String ipAddress,
        boolean resolved,
        UUID resolvedBy,
        OffsetDateTime resolvedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static SecurityIncidentResponse from(SecurityIncident incident) {
        return new SecurityIncidentResponse(
                incident.getId(),
                incident.getUserId(),
                incident.getIncidentType(),
                incident.getSeverity(),
                incident.getDescription(),
                incident.getIpAddress(),
                incident.isResolved(),
                incident.getResolvedBy(),
                incident.getResolvedAt(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        );
    }
}
