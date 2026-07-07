package com.secureauthx.server.sessions.repository;

import com.secureauthx.server.sessions.entity.Session;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastActivityAtDesc(
            UUID userId, java.time.OffsetDateTime now);

    Optional<Session> findByIdAndUserId(UUID id, UUID userId);
}
