package com.secureauthx.server.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an organization")
public record UpdateOrganizationRequest(
        @NotBlank(message = "Organization name is required.")
        @Size(min = 1, max = 255, message = "Organization name must be between 1 and 255 characters.")
        @Schema(description = "Organization name", example = "Acme Corp Updated")
        String name
) {
}
