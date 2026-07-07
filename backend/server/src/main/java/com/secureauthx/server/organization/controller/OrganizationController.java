package com.secureauthx.server.organization.controller;

import com.secureauthx.server.common.exception.ApiErrorResponse;
import com.secureauthx.server.organization.dto.CreateOrganizationRequest;
import com.secureauthx.server.organization.dto.OrganizationResponse;
import com.secureauthx.server.organization.dto.UpdateOrganizationRequest;
import com.secureauthx.server.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    @Operation(
            summary = "List user's organizations",
            description = "Returns all organizations the authenticated user belongs to.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of organizations"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<List<OrganizationResponse>> listOrganizations(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(organizationService.getUserOrganizations(userId));
    }

    @GetMapping("/current")
    @Operation(
            summary = "Get current organization",
            description = "Returns the user's personal organization.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current organization details"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No organization found",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<OrganizationResponse> getCurrentOrganization(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(organizationService.getCurrentOrganization(userId));
    }

    @PostMapping
    @Operation(
            summary = "Create an organization",
            description = "Creates a new organization. The creating user becomes the owner.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Organization created"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        OrganizationResponse response = organizationService.createOrganization(request, userId);
        return ResponseEntity
                .created(URI.create("/api/v1/organizations/" + response.id()))
                .body(response);
    }

    @PatchMapping("/{organizationId}")
    @Operation(
            summary = "Update an organization",
            description = "Updates an organization's name. Requires OWNER or ADMIN role in the organization.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Organization updated"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation failed",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Access denied",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable UUID organizationId,
            @Valid @RequestBody UpdateOrganizationRequest request,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(organizationService.updateOrganization(organizationId, request, userId));
    }
}
