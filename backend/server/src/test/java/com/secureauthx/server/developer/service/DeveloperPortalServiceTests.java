package com.secureauthx.server.developer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.developer.dto.ProjectCreateRequest;
import com.secureauthx.server.developer.dto.ProjectResponse;
import com.secureauthx.server.developer.dto.ProjectUpdateRequest;
import com.secureauthx.server.developer.entity.DeveloperProject;
import com.secureauthx.server.developer.exception.DeveloperAccessDeniedException;
import com.secureauthx.server.developer.exception.DeveloperProjectNotFoundException;
import com.secureauthx.server.developer.repository.DeveloperApiKeyRepository;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import com.secureauthx.server.oauth.entity.OAuthClient;
import com.secureauthx.server.oauth.repository.OAuthClientRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeveloperPortalServiceTests {

    @Mock private DeveloperProjectRepository projectRepository;
    @Mock private DeveloperApiKeyRepository apiKeyRepository;
    @Mock private OAuthClientRepository oauthClientRepository;
    @Mock private UserRepository userRepository;

    private DeveloperPortalService developerPortalService;

    private UUID userId;
    private User user;
    private UUID projectId;
    private DeveloperProject project;

    @BeforeEach
    void setUp() {
        developerPortalService = new DeveloperPortalService(
                projectRepository, apiKeyRepository, oauthClientRepository, userRepository
        );
        userId = UUID.randomUUID();
        user = new User("dev@example.com", "$argon2id$hash");
        setField(user, "id", userId);
        projectId = UUID.randomUUID();
        project = new DeveloperProject(userId, "My App", "A test project");
        setField(project, "id", projectId);
    }

    @Test
    void createProjectSavesAndReturnsResponse() {
        ProjectCreateRequest request = new ProjectCreateRequest("My App", "A test project", null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(DeveloperProject.class))).thenAnswer(inv -> {
            DeveloperProject p = inv.getArgument(0);
            setField(p, "id", UUID.randomUUID());
            return p;
        });

        ProjectResponse response = developerPortalService.createProject(request, userId);

        assertThat(response.name()).isEqualTo("My App");
        assertThat(response.description()).isEqualTo("A test project");
        assertThat(response.userId()).isEqualTo(userId);
        verify(projectRepository).save(any(DeveloperProject.class));
    }

    @Test
    void createProjectLinksOAuthClient() {
        UUID clientId = UUID.randomUUID();
        OAuthClient client = new OAuthClient("test-client", null, "Test", true);
        setField(client, "id", clientId);
        ProjectCreateRequest request = new ProjectCreateRequest("My App", null, clientId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(oauthClientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(projectRepository.save(any(DeveloperProject.class))).thenAnswer(inv -> {
            DeveloperProject p = inv.getArgument(0);
            setField(p, "id", UUID.randomUUID());
            return p;
        });

        ProjectResponse response = developerPortalService.createProject(request, userId);

        assertThat(response.oauthClientId()).isEqualTo(clientId);
        assertThat(client.getOwnerUserId()).isEqualTo(userId);
    }

    @Test
    void listProjectsReturnsUserProjects() {
        when(projectRepository.findByUserId(userId)).thenReturn(List.of(project));

        var projects = developerPortalService.listProjects(userId);

        assertThat(projects).hasSize(1);
        assertThat(projects.getFirst().name()).isEqualTo("My App");
    }

    @Test
    void getProjectReturnsProjectForOwner() {
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));

        ProjectResponse response = developerPortalService.getProject(projectId, userId);

        assertThat(response.name()).isEqualTo("My App");
        assertThat(response.id()).isEqualTo(projectId);
    }

    @Test
    void getProjectThrowsWhenNotOwner() {
        UUID otherUserId = UUID.randomUUID();
        when(projectRepository.findByIdAndUserId(projectId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> developerPortalService.getProject(projectId, otherUserId))
                .isInstanceOf(DeveloperProjectNotFoundException.class);
    }

    @Test
    void updateProjectUpdatesFields() {
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(DeveloperProject.class))).thenAnswer(inv -> inv.getArgument(0));

        ProjectUpdateRequest request = new ProjectUpdateRequest("Updated Name", "Updated desc");
        ProjectResponse response = developerPortalService.updateProject(projectId, request, userId);

        assertThat(response.name()).isEqualTo("Updated Name");
        assertThat(response.description()).isEqualTo("Updated desc");
    }

    @Test
    void deleteProjectDeletesOwnedProject() {
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));

        developerPortalService.deleteProject(projectId, userId);

        verify(projectRepository).delete(project);
    }

    @Test
    void deleteProjectThrowsWhenNotOwner() {
        UUID otherUserId = UUID.randomUUID();
        when(projectRepository.findByIdAndUserId(projectId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> developerPortalService.deleteProject(projectId, otherUserId))
                .isInstanceOf(DeveloperProjectNotFoundException.class);
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
