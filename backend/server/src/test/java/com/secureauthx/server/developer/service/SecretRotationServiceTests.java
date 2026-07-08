package com.secureauthx.server.developer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.developer.dto.RotateSecretResponse;
import com.secureauthx.server.developer.entity.DeveloperProject;
import com.secureauthx.server.developer.exception.DeveloperProjectNotFoundException;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import com.secureauthx.server.oauth.entity.OAuthClient;
import com.secureauthx.server.oauth.repository.OAuthClientRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class SecretRotationServiceTests {

    @Mock private DeveloperProjectRepository projectRepository;
    @Mock private OAuthClientRepository oauthClientRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private SecretRotationService secretRotationService;

    private UUID userId;
    private UUID projectId;
    private UUID clientId;
    private DeveloperProject project;
    private OAuthClient client;

    @BeforeEach
    void setUp() {
        secretRotationService = new SecretRotationService(projectRepository, oauthClientRepository, passwordEncoder);
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        project = new DeveloperProject(userId, "My App", null);
        project.setOauthClientId(clientId);
        setField(project, "id", projectId);

        client = new OAuthClient("test-client", "old-hash", "Test", true);
        setField(client, "id", clientId);
    }

    @Test
    void rotateSecretGeneratesNewSecretAndUpdatesClient() {
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(oauthClientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("$argon2id$newhash");
        when(oauthClientRepository.save(any(OAuthClient.class))).thenAnswer(inv -> inv.getArgument(0));

        RotateSecretResponse response = secretRotationService.rotateSecret(projectId, userId);

        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.oauthClientId()).isEqualTo(clientId);
        assertThat(response.newClientSecret()).isNotNull();
        assertThat(response.newClientSecret()).isNotBlank();
        assertThat(client.getHashedClientSecret()).isEqualTo("$argon2id$newhash");
        verify(oauthClientRepository).save(client);
    }

    @Test
    void rotateSecretThrowsWhenProjectNotFound() {
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> secretRotationService.rotateSecret(projectId, userId))
                .isInstanceOf(DeveloperProjectNotFoundException.class);
    }

    @Test
    void rotateSecretThrowsWhenNoOAuthClientLinked() {
        DeveloperProject noClientProject = new DeveloperProject(userId, "No Client", null);
        setField(noClientProject, "id", UUID.randomUUID());
        when(projectRepository.findByIdAndUserId(noClientProject.getId(), userId))
                .thenReturn(Optional.of(noClientProject));

        assertThatThrownBy(() -> secretRotationService.rotateSecret(noClientProject.getId(), userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no linked OAuth client");
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
