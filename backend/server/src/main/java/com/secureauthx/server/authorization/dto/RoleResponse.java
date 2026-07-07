package com.secureauthx.server.authorization.dto;

import com.secureauthx.server.authorization.entity.Role;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }
}
