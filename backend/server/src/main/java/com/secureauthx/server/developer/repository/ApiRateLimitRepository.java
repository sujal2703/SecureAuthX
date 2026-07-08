package com.secureauthx.server.developer.repository;

import com.secureauthx.server.developer.entity.ApiRateLimit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiRateLimitRepository extends JpaRepository<ApiRateLimit, UUID> {
    Optional<ApiRateLimit> findByProjectId(UUID projectId);
    void deleteByProjectId(UUID projectId);
}
