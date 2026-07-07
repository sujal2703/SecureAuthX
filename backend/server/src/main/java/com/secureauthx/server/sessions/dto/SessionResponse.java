package com.secureauthx.server.sessions.dto;

import com.secureauthx.server.sessions.entity.Session;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        OffsetDateTime createdAt,
        OffsetDateTime lastActivityAt,
        OffsetDateTime expiresAt,
        boolean isRevoked,
        boolean isExpired,
        String ipAddress,
        String deviceName,
        String operatingSystem,
        String browser,
        boolean isCurrent
) {
    public static SessionResponse from(Session session, UUID currentSessionId) {
        return new SessionResponse(
                session.getId(),
                session.getCreatedAt(),
                session.getLastActivityAt(),
                session.getExpiresAt(),
                session.isRevoked(),
                session.isExpired(),
                session.getIpAddress(),
                session.getDeviceName(),
                session.getOperatingSystem(),
                session.getBrowser(),
                session.getId().equals(currentSessionId)
        );
    }
}
