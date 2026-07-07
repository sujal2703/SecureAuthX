package com.secureauthx.server.authorization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.authorization.entity.Permission;
import com.secureauthx.server.authorization.entity.Role;
import com.secureauthx.server.authorization.entity.RolePermission;
import com.secureauthx.server.authorization.entity.UserRole;
import com.secureauthx.server.authorization.repository.RolePermissionRepository;
import com.secureauthx.server.authorization.repository.UserRoleRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

@ExtendWith(MockitoExtension.class)
class UserAuthorityServiceTests {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private UserAuthorityService userAuthorityService;

    @Test
    void loadsRoleAndPermissionAuthorities() {
        UUID userId = UUID.randomUUID();
        User user = new User("user@example.com", "$argon2id$hash");
        setField(user, "id", userId);
        Role userRole = new Role("USER", "Standard user");
        setField(userRole, "id", UUID.randomUUID());
        Permission sessionRead = new Permission("SESSION_READ", "View session details");
        setField(sessionRead, "id", UUID.randomUUID());

        when(userRoleRepository.findByUserIdWithRole(any())).thenReturn(
                List.of(new UserRole(user, userRole)));
        when(rolePermissionRepository.findByRoleIdWithPermission(userRole.getId())).thenReturn(
                List.of(new RolePermission(userRole, sessionRead)));

        List<GrantedAuthority> authorities = userAuthorityService.loadAuthorities(userId)
                .stream().toList();

        assertThat(authorities).hasSize(2);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                .containsExactlyInAnyOrder("ROLE_USER", "SESSION_READ");
    }

    @Test
    void returnsEmptyForUserWithNoRoles() {
        UUID userId = UUID.randomUUID();
        when(userRoleRepository.findByUserIdWithRole(any())).thenReturn(List.of());

        List<GrantedAuthority> authorities = userAuthorityService.loadAuthorities(userId)
                .stream().toList();

        assertThat(authorities).isEmpty();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
