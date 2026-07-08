package com.secureauthx.server.developer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(name = "RateLimitRequest")
public record RateLimitRequest(
        @Min(1) @Max(10000)
        int requestsPerMinute,

        @Min(1) @Max(1000000)
        int requestsPerHour
) {}
