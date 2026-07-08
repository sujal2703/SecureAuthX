package com.secureauthx.server.oauth.repository;

import com.secureauthx.server.oauth.entity.OAuthClient;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthClientRepository extends JpaRepository<OAuthClient, UUID> {
    Optional<OAuthClient> findByClientId(String clientId);
    boolean existsByClientId(String clientId);
}
