package com.secureauthx.server.developer.service;

import com.secureauthx.server.developer.dto.RotateSecretResponse;
import com.secureauthx.server.developer.entity.DeveloperProject;
import com.secureauthx.server.developer.exception.DeveloperProjectNotFoundException;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import com.secureauthx.server.oauth.entity.OAuthClient;
import com.secureauthx.server.oauth.repository.OAuthClientRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecretRotationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecretRotationService.class);
    private static final int SECRET_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final DeveloperProjectRepository projectRepository;
    private final OAuthClientRepository oauthClientRepository;
    private final PasswordEncoder passwordEncoder;

    public SecretRotationService(
            DeveloperProjectRepository projectRepository,
            OAuthClientRepository oauthClientRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.projectRepository = projectRepository;
        this.oauthClientRepository = oauthClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RotateSecretResponse rotateSecret(UUID projectId, UUID userId) {
        DeveloperProject project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new DeveloperProjectNotFoundException("Project not found: " + projectId));

        if (project.getOauthClientId() == null) {
            throw new IllegalStateException("Project has no linked OAuth client.");
        }

        OAuthClient client = oauthClientRepository.findById(project.getOauthClientId())
                .orElseThrow(() -> new IllegalStateException("Linked OAuth client not found."));

        byte[] secretBytes = new byte[SECRET_BYTES];
        RANDOM.nextBytes(secretBytes);
        String rawSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
        String hashedSecret = passwordEncoder.encode(rawSecret);

        client.setHashedClientSecret(hashedSecret);
        oauthClientRepository.save(client);

        LOGGER.info("OAuth client secret rotated clientId={} projectId={}", client.getClientId(), projectId);

        return new RotateSecretResponse(
                projectId,
                client.getId(),
                rawSecret,
                OffsetDateTime.now()
        );
    }
}
