package com.secureauthx.server.developer.repository;

import com.secureauthx.server.developer.entity.DeveloperApiKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeveloperApiKeyRepository extends JpaRepository<DeveloperApiKey, UUID> {
    List<DeveloperApiKey> findByProjectId(UUID projectId);
    Optional<DeveloperApiKey> findByIdAndProjectId(UUID id, UUID projectId);
    Optional<DeveloperApiKey> findByKeyHash(String keyHash);
    boolean existsByKeyHash(String keyHash);
}
