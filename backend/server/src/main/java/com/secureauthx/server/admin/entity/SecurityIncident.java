package com.secureauthx.server.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "security_incidents")
public class SecurityIncident {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "incident_type", nullable = false, length = 100)
    private String incidentType;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "resolved", nullable = false)
    private boolean resolved;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected SecurityIncident() {}

    public SecurityIncident(UUID userId, String incidentType, String severity, String description, String ipAddress) {
        this.userId = userId;
        this.incidentType = incidentType;
        this.severity = severity;
        this.description = description;
        this.ipAddress = ipAddress;
        this.resolved = false;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getIncidentType() { return incidentType; }
    public String getSeverity() { return severity; }
    public String getDescription() { return description; }
    public String getIpAddress() { return ipAddress; }
    public boolean isResolved() { return resolved; }
    public UUID getResolvedBy() { return resolvedBy; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void resolve(UUID resolvedBy) {
        this.resolved = true;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = OffsetDateTime.now();
    }
}
