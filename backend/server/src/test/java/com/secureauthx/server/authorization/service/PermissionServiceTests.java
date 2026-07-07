package com.secureauthx.server.authorization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.secureauthx.server.authorization.dto.PermissionResponse;
import com.secureauthx.server.authorization.entity.Permission;
import com.secureauthx.server.authorization.repository.PermissionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTests {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    void returnsAllPermissions() {
        Permission userRead = new Permission("USER_READ", "View user details");
        Permission userWrite = new Permission("USER_WRITE", "Create or update users");
        when(permissionRepository.findAll()).thenReturn(List.of(userRead, userWrite));

        List<PermissionResponse> permissions = permissionService.getAllPermissions();

        assertThat(permissions).hasSize(2);
        assertThat(permissions.getFirst().name()).isEqualTo("USER_READ");
        assertThat(permissions.getLast().name()).isEqualTo("USER_WRITE");
    }

    @Test
    void returnsEmptyListWhenNoPermissions() {
        when(permissionRepository.findAll()).thenReturn(List.of());

        List<PermissionResponse> permissions = permissionService.getAllPermissions();

        assertThat(permissions).isEmpty();
    }
}
