package com.secureauthx.server.admin.service;

import com.secureauthx.server.admin.dto.AnnouncementRequest;
import com.secureauthx.server.admin.dto.AnnouncementResponse;
import com.secureauthx.server.admin.entity.SystemAnnouncement;
import com.secureauthx.server.admin.exception.ResourceNotFoundException;
import com.secureauthx.server.admin.repository.SystemAnnouncementRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnnouncementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncementService.class);

    private final SystemAnnouncementRepository announcementRepository;

    public AnnouncementService(SystemAnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request, UUID adminId) {
        String severity = request.severity() != null ? request.severity().toUpperCase() : "INFO";
        SystemAnnouncement announcement = new SystemAnnouncement(
                request.title(), request.message(), severity, adminId);
        if (request.active() != null) {
            announcement.setActive(request.active());
        }
        SystemAnnouncement saved = announcementRepository.save(announcement);
        LOGGER.info("Announcement created id={} title={}", saved.getId(), saved.getTitle());
        return AnnouncementResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AnnouncementResponse::from)
                .toList();
    }

    @Transactional
    public AnnouncementResponse updateAnnouncement(UUID id, AnnouncementRequest request) {
        SystemAnnouncement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));

        if (request.title() != null) {
            announcement.setTitle(request.title());
        }
        if (request.message() != null) {
            announcement.setMessage(request.message());
        }
        if (request.severity() != null) {
            announcement.setSeverity(request.severity().toUpperCase());
        }
        if (request.active() != null) {
            announcement.setActive(request.active());
        }

        SystemAnnouncement saved = announcementRepository.save(announcement);
        LOGGER.info("Announcement updated id={}", saved.getId());
        return AnnouncementResponse.from(saved);
    }

    @Transactional
    public void deleteAnnouncement(UUID id) {
        if (!announcementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Announcement not found: " + id);
        }
        announcementRepository.deleteById(id);
        LOGGER.info("Announcement deleted id={}", id);
    }
}
