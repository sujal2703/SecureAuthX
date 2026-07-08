package com.secureauthx.server.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "IncidentResolveRequest")
public record IncidentResolveRequest(
        boolean resolved
) {}
