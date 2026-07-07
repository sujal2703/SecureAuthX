package com.secureauthx.server.authorization.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.secureauthx.server.authorization.dto.RoleResponse;
import com.secureauthx.server.authorization.entity.Role;
import com.secureauthx.server.authorization.repository.RoleRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceTests {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void returnsAllRoles() {
        Role userRole = new Role("USER", "Standard user");
        Role adminRole = new Role("ADMIN", "Administrator");
        when(roleRepository.findAll()).thenReturn(List.of(userRole, adminRole));

        List<RoleResponse> roles = roleService.getAllRoles();

        assertThat(roles).hasSize(2);
        assertThat(roles.getFirst().name()).isEqualTo("USER");
        assertThat(roles.getFirst().description()).isEqualTo("Standard user");
        assertThat(roles.getLast().name()).isEqualTo("ADMIN");
    }

    @Test
    void returnsEmptyListWhenNoRoles() {
        when(roleRepository.findAll()).thenReturn(List.of());

        List<RoleResponse> roles = roleService.getAllRoles();

        assertThat(roles).isEmpty();
    }
}
