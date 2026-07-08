package com.secureauthx.server.oauth.dto;

import com.secureauthx.server.oauth.entity.OAuthClient;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(name = "ClientResponse")
public record ClientResponse(
        UUID id,
        String clientId,
        String clientName,
        boolean confidential,
        boolean enabled,
        List<String> redirectUris,
        OffsetDateTime createdAt
) {
    public static ClientResponse from(OAuthClient client, List<String> redirectUris) {
        return new ClientResponse(
                client.getId(),
                client.getClientId(),
                client.getClientName(),
                client.isConfidential(),
                client.isEnabled(),
                redirectUris,
                client.getCreatedAt()
        );
    }
}
