package com.secureauthx.server.oauth.service;

import com.secureauthx.server.admin.service.AuditService;
import com.secureauthx.server.oauth.dto.ClientResponse;
import com.secureauthx.server.oauth.dto.CreateClientRequest;
import com.secureauthx.server.oauth.dto.CreateClientResponse;
import com.secureauthx.server.oauth.entity.OAuthClient;
import com.secureauthx.server.oauth.entity.OAuthClientRedirectUri;
import com.secureauthx.server.oauth.exception.InvalidClientException;
import com.secureauthx.server.oauth.repository.OAuthClientRedirectUriRepository;
import com.secureauthx.server.oauth.repository.OAuthClientRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuthClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthClientService.class);

    @Autowired(required = false)
    private AuditService auditService;

    private final OAuthClientRepository oauthClientRepository;
    private final OAuthClientRedirectUriRepository redirectUriRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuthClientService(
            OAuthClientRepository oauthClientRepository,
            OAuthClientRedirectUriRepository redirectUriRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.oauthClientRepository = oauthClientRepository;
        this.redirectUriRepository = redirectUriRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CreateClientResponse createClient(CreateClientRequest request) {
        if (oauthClientRepository.existsByClientId(request.clientId())) {
            throw new InvalidClientException("Client ID already exists: " + request.clientId());
        }

        String hashedSecret = null;
        String rawSecret = request.clientSecret();
        if (request.confidential() && (rawSecret == null || rawSecret.isBlank())) {
            throw new InvalidClientException("Confidential clients must have a client secret.");
        }
        if (rawSecret != null && !rawSecret.isBlank()) {
            hashedSecret = passwordEncoder.encode(rawSecret);
        }

        OAuthClient client = new OAuthClient(
                request.clientId(),
                hashedSecret,
                request.clientName(),
                request.confidential()
        );
        OAuthClient saved = oauthClientRepository.save(client);

        List<OAuthClientRedirectUri> redirectUris = request.redirectUris().stream()
                .map(uri -> new OAuthClientRedirectUri(saved, uri))
                .toList();
        redirectUriRepository.saveAll(redirectUris);

        LOGGER.info("OAuth client created client_id={} id={}", saved.getClientId(), saved.getId());

        if (auditService != null) {
            auditService.record(null, null, "OAUTH_CLIENT_CREATED", saved.getClientId(), true, null);
        }

        return new CreateClientResponse(
                saved.getId(),
                saved.getClientId(),
                rawSecret,
                saved.getClientName(),
                saved.isConfidential(),
                saved.isEnabled(),
                request.redirectUris(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        return oauthClientRepository.findAll().stream()
                .map(client -> {
                    List<String> uris = redirectUriRepository.findByClientId(client.getId())
                            .stream()
                            .map(OAuthClientRedirectUri::getRedirectUri)
                            .toList();
                    return ClientResponse.from(client, uris);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientResponse getClient(UUID id) {
        OAuthClient client = oauthClientRepository.findById(id)
                .orElseThrow(() -> new InvalidClientException("Client not found: " + id));
        List<String> uris = redirectUriRepository.findByClientId(client.getId())
                .stream()
                .map(OAuthClientRedirectUri::getRedirectUri)
                .toList();
        return ClientResponse.from(client, uris);
    }

    @Transactional(readOnly = true)
    public OAuthClient getOAuthClientByClientId(String clientId) {
        return oauthClientRepository.findByClientId(clientId)
                .orElseThrow(() -> new InvalidClientException("Client not found: " + clientId));
    }

    @Transactional(readOnly = true)
    public OAuthClient authenticateClient(String clientId, String clientSecret) {
        OAuthClient client = getOAuthClientByClientId(clientId);

        if (!client.isEnabled()) {
            throw new InvalidClientException("Client is disabled: " + clientId);
        }

        if (client.isConfidential()) {
            if (clientSecret == null || clientSecret.isBlank()) {
                throw new InvalidClientException("Client secret is required for confidential clients.");
            }
            if (!passwordEncoder.matches(clientSecret, client.getHashedClientSecret())) {
                throw new InvalidClientException("Invalid client secret.");
            }
        }

        return client;
    }
}
