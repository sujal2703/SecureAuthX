package com.secureauthx.server.authorization.controller;

import com.secureauthx.server.authorization.dto.PermissionResponse;
import com.secureauthx.server.authorization.service.PermissionService;
import com.secureauthx.server.common.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Authorization")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @Operation(
            summary = "List all permissions",
            description = "Returns all available permissions. Requires authentication.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of permissions"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }
}
