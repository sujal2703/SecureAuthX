package com.secureauthx.server.developer.repository;

import com.secureauthx.server.developer.entity.DeveloperApiUsage;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeveloperApiUsageRepository extends JpaRepository<DeveloperApiUsage, UUID> {
    List<DeveloperApiUsage> findByProjectIdAndDateBetweenOrderByDateAsc(UUID projectId, LocalDate start, LocalDate end);
    Optional<DeveloperApiUsage> findByProjectIdAndDate(UUID projectId, LocalDate date);
}
