package com.secureauthx.server.oauth.controller;

import com.secureauthx.server.oauth.dto.ClientResponse;
import com.secureauthx.server.oauth.dto.CreateClientRequest;
import com.secureauthx.server.oauth.dto.CreateClientResponse;
import com.secureauthx.server.oauth.service.OAuthClientService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oauth/clients")
@PreAuthorize("hasRole('ADMIN')")
public class OAuthClientController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthClientController.class);

    private final OAuthClientService oauthClientService;

    public OAuthClientController(OAuthClientService oauthClientService) {
        this.oauthClientService = oauthClientService;
    }

    @PostMapping
    public ResponseEntity<CreateClientResponse> createClient(@Valid @RequestBody CreateClientRequest request) {
        CreateClientResponse response = oauthClientService.createClient(request);
        LOGGER.info("OAuth client created: {}", response.clientId());
        return ResponseEntity
                .created(URI.create("/api/v1/oauth/clients/" + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        List<ClientResponse> clients = oauthClientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable UUID id) {
        ClientResponse client = oauthClientService.getClient(id);
        return ResponseEntity.ok(client);
    }
}
