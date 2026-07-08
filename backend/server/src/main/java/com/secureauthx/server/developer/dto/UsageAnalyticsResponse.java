package com.secureauthx.server.developer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(name = "UsageAnalyticsResponse")
public record UsageAnalyticsResponse(
        UUID id,
        UUID projectId,
        LocalDate date,
        long requestCount,
        long successCount,
        long failureCount,
        double avgLatencyMs,
        OffsetDateTime lastRequestAt,
        long tokenExchanges,
        long userinfoRequests
) {}
