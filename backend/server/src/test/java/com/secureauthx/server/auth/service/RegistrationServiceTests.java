package com.secureauthx.server.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.auth.dto.RegistrationRequest;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.exception.DuplicateEmailException;
import com.secureauthx.server.auth.mapper.UserMapper;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.authorization.entity.Role;
import com.secureauthx.server.authorization.entity.UserRole;
import com.secureauthx.server.authorization.repository.RoleRepository;
import com.secureauthx.server.authorization.repository.UserRoleRepository;
import com.secureauthx.server.organization.entity.Organization;
import com.secureauthx.server.organization.service.OrganizationService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void normalizesEmailAndStoresPasswordHash() {
        RegistrationRequest request = new RegistrationRequest("  Developer@Example.COM ", "S3cureExample!2026");
        when(userRepository.existsByEmail("developer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("S3cureExample!2026")).thenReturn("$argon2id$hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(new Role("USER", "Standard user")));
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(organizationService.createPersonalOrganization(any()))
                .thenReturn(new Organization("Test", "test", true));

        registrationService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(savedUser.getEmail()).isEqualTo("developer@example.com");
        org.assertj.core.api.Assertions.assertThat(savedUser.getPasswordHash()).isEqualTo("$argon2id$hash");
        verify(userRoleRepository).save(any(UserRole.class));
        verify(organizationService).createPersonalOrganization(any());
    }

    @Test
    void rejectsDuplicateEmailBeforeHashingPassword() {
        RegistrationRequest request = new RegistrationRequest("developer@example.com", "S3cureExample!2026");
        when(userRepository.existsByEmail("developer@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
    }
}
