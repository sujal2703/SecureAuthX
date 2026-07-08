package com.secureauthx.server.admin.repository;

import com.secureauthx.server.admin.entity.SecurityIncident;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityIncidentRepository extends JpaRepository<SecurityIncident, UUID> {
    Page<SecurityIncident> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<SecurityIncident> findByResolvedOrderByCreatedAtDesc(boolean resolved, Pageable pageable);
    long countByResolved(boolean resolved);
    List<SecurityIncident> findByUserIdAndIncidentTypeAndCreatedAtBetween(
            UUID userId, String incidentType, OffsetDateTime start, OffsetDateTime end);
    long count();
}
