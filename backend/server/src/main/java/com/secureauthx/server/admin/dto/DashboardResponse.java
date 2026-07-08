package com.secureauthx.server.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DashboardResponse")
public record DashboardResponse(
        long totalUsers,
        long totalOrganizations,
        long totalOAuthClients,
        long totalPasskeys,
        long totalSessions,
        long totalLoginEvents,
        long activeSessions,
        long securityIncidents,
        long developerProjects
) {}
