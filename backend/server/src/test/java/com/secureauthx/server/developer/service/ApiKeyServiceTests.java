package com.secureauthx.server.developer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.developer.dto.ApiKeyResponse;
import com.secureauthx.server.developer.dto.CreateApiKeyRequest;
import com.secureauthx.server.developer.dto.CreateApiKeyResponse;
import com.secureauthx.server.developer.entity.DeveloperApiKey;
import com.secureauthx.server.developer.entity.DeveloperProject;
import com.secureauthx.server.developer.exception.DeveloperApiKeyNotFoundException;
import com.secureauthx.server.developer.exception.DeveloperProjectNotFoundException;
import com.secureauthx.server.developer.repository.DeveloperApiKeyRepository;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTests {

    @Mock private DeveloperProjectRepository projectRepository;
    @Mock private DeveloperApiKeyRepository apiKeyRepository;

    private ApiKeyService apiKeyService;

    private UUID userId;
    private UUID projectId;
    private DeveloperProject project;
    private UUID keyId;
    private DeveloperApiKey apiKey;

    @BeforeEach
    void setUp() {
        apiKeyService = new ApiKeyService(projectRepository, apiKeyRepository);
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        project = new DeveloperProject(userId, "My App", null);
        setField(project, "id", projectId);
        keyId = UUID.randomUUID();
        apiKey = new DeveloperApiKey(projectId, "abc123hash", "sk_abc1", "test-key", null);
        setField(apiKey, "id", keyId);
    }

    @Test
    void createApiKeyGeneratesKeyAndReturnsResponse() {
        CreateApiKeyRequest request = new CreateApiKeyRequest("my-key", null);
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(apiKeyRepository.save(any(DeveloperApiKey.class))).thenAnswer(inv -> {
            DeveloperApiKey key = inv.getArgument(0);
            setField(key, "id", UUID.randomUUID());
            return key;
        });

        CreateApiKeyResponse response = apiKeyService.createApiKey(projectId, request, userId);

        assertThat(response.label()).isEqualTo("my-key");
        assertThat(response.plainTextKey()).startsWith("sk_");
        assertThat(response.keyPrefix()).hasSize(8);
        verify(apiKeyRepository).save(any(DeveloperApiKey.class));
    }

    @Test
    void createApiKeyThrowsWhenProjectNotFound() {
        UUID otherId = UUID.randomUUID();
        when(projectRepository.findByIdAndUserId(projectId, otherId)).thenReturn(Optional.empty());

        CreateApiKeyRequest request = new CreateApiKeyRequest("key", null);
        assertThatThrownBy(() -> apiKeyService.createApiKey(projectId, request, otherId))
                .isInstanceOf(DeveloperProjectNotFoundException.class);
    }

    @Test
    void listApiKeysReturnsKeysForProject() {
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(apiKeyRepository.findByProjectId(projectId)).thenReturn(List.of(apiKey));

        List<ApiKeyResponse> keys = apiKeyService.listApiKeys(projectId, userId);

        assertThat(keys).hasSize(1);
        assertThat(keys.getFirst().label()).isEqualTo("test-key");
    }

    @Test
    void revokeApiKeyDisablesKey() {
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(apiKeyRepository.findByIdAndProjectId(keyId, projectId)).thenReturn(Optional.of(apiKey));
        when(apiKeyRepository.save(any(DeveloperApiKey.class))).thenAnswer(inv -> inv.getArgument(0));

        apiKeyService.revokeApiKey(projectId, keyId, userId);

        assertThat(apiKey.isEnabled()).isFalse();
    }

    @Test
    void revokeApiKeyThrowsWhenKeyNotFound() {
        UUID badKeyId = UUID.randomUUID();
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(apiKeyRepository.findByIdAndProjectId(badKeyId, projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apiKeyService.revokeApiKey(projectId, badKeyId, userId))
                .isInstanceOf(DeveloperApiKeyNotFoundException.class);
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
