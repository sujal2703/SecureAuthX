package com.secureauthx.server.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.admin.dto.AuditLogResponse;
import com.secureauthx.server.admin.entity.AuditLog;
import com.secureauthx.server.admin.repository.AuditLogRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AuditServiceTests {

    @Mock private AuditLogRepository auditLogRepository;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditLogRepository);
    }

    @Test
    void recordSavesAuditLog() {
        UUID userId = UUID.randomUUID();
        auditService.record(userId, null, "LOGIN", "user@example.com", true, null);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void getAuditLogReturnsLog() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AuditLog log = new AuditLog(userId, null, "127.0.0.1", "LOGIN", "user@example.com", true, null);
        setField(log, "id", id);

        when(auditLogRepository.findById(id)).thenReturn(Optional.of(log));

        AuditLogResponse response = auditService.getAuditLog(id);
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.action()).isEqualTo("LOGIN");
    }

    @Test
    void getAuditLogsWithPaginationReturnsPage() {
        UUID userId = UUID.randomUUID();
        Page<AuditLog> page = new PageImpl<>(List.of());
        when(auditLogRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);

        Page<AuditLogResponse> result = auditService.getAuditLogs(null, null, null, null, 0, 20);
        assertThat(result).isEmpty();
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
