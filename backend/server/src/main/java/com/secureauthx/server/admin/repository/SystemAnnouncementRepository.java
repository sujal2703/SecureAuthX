package com.secureauthx.server.admin.repository;

import com.secureauthx.server.admin.entity.SystemAnnouncement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemAnnouncementRepository extends JpaRepository<SystemAnnouncement, UUID> {
    List<SystemAnnouncement> findByActiveTrueOrderByCreatedAtDesc();
    List<SystemAnnouncement> findAllByOrderByCreatedAtDesc();
}
