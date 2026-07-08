package com.secureauthx.server.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.admin.dto.AnnouncementRequest;
import com.secureauthx.server.admin.dto.AnnouncementResponse;
import com.secureauthx.server.admin.entity.SystemAnnouncement;
import com.secureauthx.server.admin.exception.ResourceNotFoundException;
import com.secureauthx.server.admin.repository.SystemAnnouncementRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTests {

    @Mock private SystemAnnouncementRepository announcementRepository;

    private AnnouncementService announcementService;

    @BeforeEach
    void setUp() {
        announcementService = new AnnouncementService(announcementRepository);
    }

    @Test
    void createAnnouncementSavesAndReturns() {
        UUID adminId = UUID.randomUUID();
        AnnouncementRequest request = new AnnouncementRequest("System Update", "Scheduled maintenance", "INFO", true);
        when(announcementRepository.save(any(SystemAnnouncement.class))).thenAnswer(inv -> {
            SystemAnnouncement a = inv.getArgument(0);
            setField(a, "id", UUID.randomUUID());
            return a;
        });

        AnnouncementResponse response = announcementService.createAnnouncement(request, adminId);
        assertThat(response.title()).isEqualTo("System Update");
        assertThat(response.severity()).isEqualTo("INFO");
    }

    @Test
    void listAnnouncementsReturnsAll() {
        UUID adminId = UUID.randomUUID();
        SystemAnnouncement announcement = new SystemAnnouncement("Title", "Message", "INFO", adminId);
        setField(announcement, "id", UUID.randomUUID());
        when(announcementRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(announcement));

        var result = announcementService.listAnnouncements();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("Title");
    }

    @Test
    void updateAnnouncementUpdatesFields() {
        UUID id = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        SystemAnnouncement announcement = new SystemAnnouncement("Old Title", "Old Message", "INFO", adminId);
        setField(announcement, "id", id);

        when(announcementRepository.findById(id)).thenReturn(Optional.of(announcement));
        when(announcementRepository.save(any(SystemAnnouncement.class))).thenAnswer(inv -> inv.getArgument(0));

        AnnouncementRequest request = new AnnouncementRequest("New Title", "New Message", "WARNING", null);
        AnnouncementResponse response = announcementService.updateAnnouncement(id, request);

        assertThat(response.title()).isEqualTo("New Title");
        assertThat(response.message()).isEqualTo("New Message");
        assertThat(response.severity()).isEqualTo("WARNING");
    }

    @Test
    void deleteAnnouncementRemovesIt() {
        UUID id = UUID.randomUUID();
        when(announcementRepository.existsById(id)).thenReturn(true);
        announcementService.deleteAnnouncement(id);
        verify(announcementRepository).deleteById(id);
    }

    @Test
    void deleteAnnouncementThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(announcementRepository.existsById(id)).thenReturn(false);
        assertThatThrownBy(() -> announcementService.deleteAnnouncement(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
