package com.secureauthx.server.admin.controller;

import com.secureauthx.server.admin.dto.AnnouncementRequest;
import com.secureauthx.server.admin.dto.AnnouncementResponse;
import com.secureauthx.server.admin.dto.AuditLogResponse;
import com.secureauthx.server.admin.dto.DashboardResponse;
import com.secureauthx.server.admin.dto.IncidentResolveRequest;
import com.secureauthx.server.admin.dto.SecurityIncidentResponse;
import com.secureauthx.server.admin.dto.SystemSettingRequest;
import com.secureauthx.server.admin.dto.SystemSettingResponse;
import com.secureauthx.server.admin.service.AnnouncementService;
import com.secureauthx.server.admin.service.AuditService;
import com.secureauthx.server.admin.service.DashboardService;
import com.secureauthx.server.admin.service.IncidentService;
import com.secureauthx.server.admin.service.SystemSettingsService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final AuditService auditService;
    private final AnnouncementService announcementService;
    private final SystemSettingsService systemSettingsService;
    private final IncidentService incidentService;

    public AdminController(
            DashboardService dashboardService,
            AuditService auditService,
            AnnouncementService announcementService,
            SystemSettingsService systemSettingsService,
            IncidentService incidentService
    ) {
        this.dashboardService = dashboardService;
        this.auditService = auditService;
        this.announcementService = announcementService;
        this.systemSettingsService = systemSettingsService;
        this.incidentService = incidentService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/audit")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) OffsetDateTime startDate,
            @RequestParam(required = false) OffsetDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuditLogResponse> logs = auditService.getAuditLogs(userId, action, startDate, endDate, page, size);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/audit/{id}")
    public ResponseEntity<AuditLogResponse> getAuditLog(@PathVariable UUID id) {
        return ResponseEntity.ok(auditService.getAuditLog(id));
    }

    @PostMapping("/announcements")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @Valid @RequestBody AnnouncementRequest request,
            Principal principal
    ) {
        AnnouncementResponse response = announcementService.createAnnouncement(
                request, UUID.fromString(principal.getName()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementResponse>> listAnnouncements() {
        return ResponseEntity.ok(announcementService.listAnnouncements());
    }

    @PatchMapping("/announcements/{id}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            @PathVariable UUID id,
            @Valid @RequestBody AnnouncementRequest request
    ) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, request));
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable UUID id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/settings")
    public ResponseEntity<List<SystemSettingResponse>> getSettings() {
        return ResponseEntity.ok(systemSettingsService.getAllSettings());
    }

    @PutMapping("/settings/{key}")
    public ResponseEntity<SystemSettingResponse> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody SystemSettingRequest request
    ) {
        return ResponseEntity.ok(systemSettingsService.updateSetting(key, request));
    }

    @GetMapping("/incidents")
    public ResponseEntity<Page<SecurityIncidentResponse>> getIncidents(
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(incidentService.getIncidents(resolved, page, size));
    }

    @GetMapping("/incidents/{id}")
    public ResponseEntity<SecurityIncidentResponse> getIncident(@PathVariable UUID id) {
        return ResponseEntity.ok(incidentService.getIncident(id));
    }

    @PatchMapping("/incidents/{id}")
    public ResponseEntity<SecurityIncidentResponse> resolveIncident(
            @PathVariable UUID id,
            @RequestBody IncidentResolveRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(incidentService.resolveIncident(
                id, request, UUID.fromString(principal.getName())));
    }
}
