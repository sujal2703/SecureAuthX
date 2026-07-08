package com.secureauthx.server.developer.service;

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
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeveloperPortalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeveloperPortalService.class);

    private final DeveloperProjectRepository projectRepository;
    private final DeveloperApiKeyRepository apiKeyRepository;
    private final OAuthClientRepository oauthClientRepository;
    private final UserRepository userRepository;

    public DeveloperPortalService(
            DeveloperProjectRepository projectRepository,
            DeveloperApiKeyRepository apiKeyRepository,
            OAuthClientRepository oauthClientRepository,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.oauthClientRepository = oauthClientRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DeveloperAccessDeniedException("User not found."));

        if (request.oauthClientId() != null) {
            OAuthClient client = oauthClientRepository.findById(request.oauthClientId())
                    .orElseThrow(() -> new IllegalArgumentException("OAuth client not found."));
            client.setOwnerUserId(userId);
            oauthClientRepository.save(client);
        }

        DeveloperProject project = new DeveloperProject(userId, request.name(), request.description());
        project.setOauthClientId(request.oauthClientId());
        DeveloperProject saved = projectRepository.save(project);

        LOGGER.info("Developer project created id={} userId={}", saved.getId(), userId);
        return ProjectResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects(UUID userId) {
        return projectRepository.findByUserId(userId).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID projectId, UUID userId) {
        DeveloperProject project = findProjectForUser(projectId, userId);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, ProjectUpdateRequest request, UUID userId) {
        DeveloperProject project = findProjectForUser(projectId, userId);

        if (request.name() != null) {
            project.setName(request.name());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }

        DeveloperProject saved = projectRepository.save(project);
        LOGGER.info("Developer project updated id={}", saved.getId());
        return ProjectResponse.from(saved);
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID userId) {
        DeveloperProject project = findProjectForUser(projectId, userId);
        projectRepository.delete(project);
        LOGGER.info("Developer project deleted id={}", projectId);
    }

    private DeveloperProject findProjectForUser(UUID projectId, UUID userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new DeveloperProjectNotFoundException(
                        "Project not found: " + projectId));
    }
}
