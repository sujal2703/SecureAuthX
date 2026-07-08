package com.secureauthx.server.admin.dto;

import com.secureauthx.server.admin.entity.AuditLog;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "AuditLogResponse")
public record AuditLogResponse(
        UUID id,
        UUID userId,
        UUID organizationId,
        String ipAddress,
        String action,
        String target,
        boolean success,
        String details,
        OffsetDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUserId(),
                log.getOrganizationId(),
                log.getIpAddress(),
                log.getAction(),
                log.getTarget(),
                log.isSuccess(),
                log.getDetails(),
                log.getCreatedAt()
        );
    }
}
