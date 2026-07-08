package com.secureauthx.server.developer.service;

import com.secureauthx.server.developer.dto.ApiKeyResponse;
import com.secureauthx.server.developer.dto.CreateApiKeyRequest;
import com.secureauthx.server.developer.dto.CreateApiKeyResponse;
import com.secureauthx.server.developer.entity.DeveloperApiKey;
import com.secureauthx.server.developer.entity.DeveloperProject;
import com.secureauthx.server.developer.exception.DeveloperApiKeyNotFoundException;
import com.secureauthx.server.developer.exception.DeveloperProjectNotFoundException;
import com.secureauthx.server.developer.repository.DeveloperApiKeyRepository;
import com.secureauthx.server.developer.repository.DeveloperProjectRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiKeyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyService.class);
    private static final int KEY_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final DeveloperProjectRepository projectRepository;
    private final DeveloperApiKeyRepository apiKeyRepository;

    public ApiKeyService(
            DeveloperProjectRepository projectRepository,
            DeveloperApiKeyRepository apiKeyRepository
    ) {
        this.projectRepository = projectRepository;
        this.apiKeyRepository = apiKeyRepository;
    }

    @Transactional
    public CreateApiKeyResponse createApiKey(UUID projectId, CreateApiKeyRequest request, UUID userId) {
        DeveloperProject project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new DeveloperProjectNotFoundException("Project not found: " + projectId));

        byte[] keyBytes = new byte[KEY_BYTES];
        RANDOM.nextBytes(keyBytes);
        String rawKey = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
        String prefix = rawKey.substring(0, 8);
        String hash = sha256Hex(rawKey);

        DeveloperApiKey apiKey = new DeveloperApiKey(projectId, hash, prefix, request.label(), request.expiresAt());
        DeveloperApiKey saved = apiKeyRepository.save(apiKey);

        LOGGER.info("API key created id={} projectId={}", saved.getId(), projectId);

        return new CreateApiKeyResponse(
                saved.getId(),
                saved.getProjectId(),
                saved.getKeyPrefix(),
                "sk_" + rawKey,
                saved.getLabel(),
                saved.getExpiresAt(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> listApiKeys(UUID projectId, UUID userId) {
        projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new DeveloperProjectNotFoundException("Project not found: " + projectId));
        return apiKeyRepository.findByProjectId(projectId).stream()
                .map(ApiKeyResponse::from)
                .toList();
    }

    @Transactional
    public void revokeApiKey(UUID projectId, UUID keyId, UUID userId) {
        projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new DeveloperProjectNotFoundException("Project not found: " + projectId));
        DeveloperApiKey key = apiKeyRepository.findByIdAndProjectId(keyId, projectId)
                .orElseThrow(() -> new DeveloperApiKeyNotFoundException("API key not found: " + keyId));
        key.setEnabled(false);
        apiKeyRepository.save(key);
        LOGGER.info("API key revoked id={}", keyId);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
