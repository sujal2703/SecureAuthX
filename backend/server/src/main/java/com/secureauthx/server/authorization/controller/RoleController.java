package com.secureauthx.server.authorization.controller;

import com.secureauthx.server.authorization.dto.RoleResponse;
import com.secureauthx.server.authorization.service.RoleService;
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
@RequestMapping("/api/v1/roles")
@Tag(name = "Authorization")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @Operation(
            summary = "List all roles",
            description = "Returns all available roles. Requires authentication.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of roles"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }
}
