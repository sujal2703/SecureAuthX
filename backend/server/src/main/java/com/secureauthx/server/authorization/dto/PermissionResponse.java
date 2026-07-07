package com.secureauthx.server.authorization.dto;

import com.secureauthx.server.authorization.entity.Permission;
import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String name,
        String description
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getName(), permission.getDescription());
    }
}
