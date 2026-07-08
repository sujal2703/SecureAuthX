package com.secureauthx.server.oauth.repository;

import com.secureauthx.server.oauth.entity.OAuthClientRedirectUri;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthClientRedirectUriRepository extends JpaRepository<OAuthClientRedirectUri, UUID> {
    List<OAuthClientRedirectUri> findByClientId(UUID clientId);
}
