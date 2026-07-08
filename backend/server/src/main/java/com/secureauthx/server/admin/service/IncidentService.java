package com.secureauthx.server.admin.service;

import com.secureauthx.server.admin.dto.IncidentResolveRequest;
import com.secureauthx.server.admin.dto.SecurityIncidentResponse;
import com.secureauthx.server.admin.entity.SecurityIncident;
import com.secureauthx.server.admin.exception.ResourceNotFoundException;
import com.secureauthx.server.admin.repository.SecurityIncidentRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncidentService.class);

    private final SecurityIncidentRepository incidentRepository;

    public IncidentService(SecurityIncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public void recordIncident(UUID userId, String incidentType, String severity,
                                String description, String ipAddress) {
        SecurityIncident incident = new SecurityIncident(userId, incidentType, severity, description, ipAddress);
        incidentRepository.save(incident);
        LOGGER.warn("Security incident recorded type={} userId={} severity={}", incidentType, userId, severity);
    }

    @Transactional(readOnly = true)
    public Page<SecurityIncidentResponse> getIncidents(Boolean resolved, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SecurityIncident> incidents;

        if (resolved != null) {
            incidents = incidentRepository.findByResolvedOrderByCreatedAtDesc(resolved, pageable);
        } else {
            incidents = incidentRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return incidents.map(SecurityIncidentResponse::from);
    }

    @Transactional(readOnly = true)
    public SecurityIncidentResponse getIncident(UUID id) {
        SecurityIncident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));
        return SecurityIncidentResponse.from(incident);
    }

    @Transactional
    public SecurityIncidentResponse resolveIncident(UUID id, IncidentResolveRequest request, UUID adminId) {
        SecurityIncident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found: " + id));

        if (request.resolved()) {
            incident.resolve(adminId);
        }

        SecurityIncident saved = incidentRepository.save(incident);
        LOGGER.info("Incident resolved id={} by adminId={}", id, adminId);
        return SecurityIncidentResponse.from(saved);
    }
}
