package com.secureauthx.server.authorization.repository;

import com.secureauthx.server.authorization.entity.RolePermission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    @Query("SELECT rp FROM RolePermission rp JOIN FETCH rp.permission WHERE rp.role.id = :roleId")
    List<RolePermission> findByRoleIdWithPermission(@Param("roleId") UUID roleId);

    List<RolePermission> findByPermissionId(UUID permissionId);
}
