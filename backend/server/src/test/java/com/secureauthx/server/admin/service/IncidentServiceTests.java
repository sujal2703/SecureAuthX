package com.secureauthx.server.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.admin.dto.IncidentResolveRequest;
import com.secureauthx.server.admin.dto.SecurityIncidentResponse;
import com.secureauthx.server.admin.entity.SecurityIncident;
import com.secureauthx.server.admin.exception.ResourceNotFoundException;
import com.secureauthx.server.admin.repository.SecurityIncidentRepository;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
class IncidentServiceTests {

    @Mock private SecurityIncidentRepository incidentRepository;

    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        incidentService = new IncidentService(incidentRepository);
    }

    @Test
    void recordIncidentSavesIncident() {
        incidentService.recordIncident(UUID.randomUUID(), "HIGH_RISK_LOGIN", "HIGH", "Test incident", "192.168.1.1");
        verify(incidentRepository).save(any(SecurityIncident.class));
    }

    @Test
    void getIncidentsReturnsPage() {
        Page<SecurityIncident> page = new PageImpl<>(List.of());
        when(incidentRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);

        Page<SecurityIncidentResponse> result = incidentService.getIncidents(null, 0, 20);
        assertThat(result).isEmpty();
    }

    @Test
    void getIncidentReturnsById() {
        UUID id = UUID.randomUUID();
        SecurityIncident incident = new SecurityIncident(UUID.randomUUID(), "MULTIPLE_FAILED_LOGINS", "MEDIUM", "desc", "1.2.3.4");
        setField(incident, "id", id);
        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));

        SecurityIncidentResponse response = incidentService.getIncident(id);
        assertThat(response.incidentType()).isEqualTo("MULTIPLE_FAILED_LOGINS");
    }

    @Test
    void resolveIncidentMarksResolved() {
        UUID adminId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        SecurityIncident incident = new SecurityIncident(UUID.randomUUID(), "SUSPICIOUS_OAUTH", "HIGH", "desc", null);
        setField(incident, "id", id);

        when(incidentRepository.findById(id)).thenReturn(Optional.of(incident));
        when(incidentRepository.save(any(SecurityIncident.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidentResolveRequest request = new IncidentResolveRequest(true);
        SecurityIncidentResponse response = incidentService.resolveIncident(id, request, adminId);

        assertThat(response.resolved()).isTrue();
        assertThat(response.resolvedBy()).isEqualTo(adminId);
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
