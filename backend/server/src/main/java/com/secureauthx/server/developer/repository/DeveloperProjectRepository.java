package com.secureauthx.server.developer.repository;

import com.secureauthx.server.developer.entity.DeveloperProject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeveloperProjectRepository extends JpaRepository<DeveloperProject, UUID> {
    List<DeveloperProject> findByUserId(UUID userId);
    Optional<DeveloperProject> findByIdAndUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);
}
