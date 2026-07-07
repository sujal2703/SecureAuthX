package com.secureauthx.server.authorization.service;

import com.secureauthx.server.authorization.entity.RolePermission;
import com.secureauthx.server.authorization.entity.UserRole;
import com.secureauthx.server.authorization.repository.RolePermissionRepository;
import com.secureauthx.server.authorization.repository.UserRoleRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class UserAuthorityService {

    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public UserAuthorityService(
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository
    ) {
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public Collection<GrantedAuthority> loadAuthorities(UUID userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRole(userId);

        Stream<GrantedAuthority> roleAuthorities = userRoles.stream()
                .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRole().getName()));

        Stream<GrantedAuthority> permissionAuthorities = userRoles.stream()
                .flatMap(ur -> rolePermissionRepository.findByRoleIdWithPermission(ur.getRole().getId()).stream())
                .map(rp -> new SimpleGrantedAuthority(rp.getPermission().getName()));

        return Stream.concat(roleAuthorities, permissionAuthorities)
                .distinct()
                .toList();
    }
}
