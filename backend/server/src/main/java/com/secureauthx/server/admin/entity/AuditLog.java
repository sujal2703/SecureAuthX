package com.secureauthx.server.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "target", length = 500)
    private String target;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected AuditLog() {}

    public AuditLog(UUID userId, UUID organizationId, String ipAddress, String action, String target, boolean success, String details) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.ipAddress = ipAddress;
        this.action = action;
        this.target = target;
        this.success = success;
        this.details = details;
    }

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getOrganizationId() { return organizationId; }
    public String getIpAddress() { return ipAddress; }
    public String getAction() { return action; }
    public String getTarget() { return target; }
    public boolean isSuccess() { return success; }
    public String getDetails() { return details; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
