package com.secureauthx.server.admin.service;

import com.secureauthx.server.admin.dto.AuditLogResponse;
import com.secureauthx.server.admin.entity.AuditLog;
import com.secureauthx.server.admin.exception.ResourceNotFoundException;
import com.secureauthx.server.admin.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(UUID userId, UUID organizationId, String action, String target, boolean success, String details) {
        String ipAddress = resolveIp();
        AuditLog log = new AuditLog(userId, organizationId, ipAddress, action, target, success, details);
        auditLogRepository.save(log);
        LOGGER.debug("Audit recorded action={} userId={} success={}", action, userId, success);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(UUID userId, String action, OffsetDateTime startDate,
                                                OffsetDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs;

        if (userId != null) {
            logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        } else if (action != null) {
            logs = auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable);
        } else if (startDate != null && endDate != null) {
            logs = auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable);
        } else {
            logs = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return logs.map(AuditLogResponse::from);
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLog(UUID id) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found: " + id));
        return AuditLogResponse.from(log);
    }

    @Transactional(readOnly = true)
    public long countLogins() {
        return auditLogRepository.findByActionOrderByCreatedAtDesc("LOGIN", Pageable.unpaged()).getTotalElements();
    }

    private String resolveIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isBlank()) {
                return ip.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "unknown";
    }
}
