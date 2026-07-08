package com.secureauthx.server.developer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "developer_api_usage")
public class DeveloperApiUsage {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "request_count", nullable = false)
    private long requestCount;

    @Column(name = "success_count", nullable = false)
    private long successCount;

    @Column(name = "failure_count", nullable = false)
    private long failureCount;

    @Column(name = "avg_latency_ms", nullable = false)
    private double avgLatencyMs;

    @Column(name = "last_request_at")
    private OffsetDateTime lastRequestAt;

    @Column(name = "token_exchanges", nullable = false)
    private long tokenExchanges;

    @Column(name = "userinfo_requests", nullable = false)
    private long userinfoRequests;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected DeveloperApiUsage() {}

    public DeveloperApiUsage(UUID projectId, LocalDate date) {
        this.projectId = projectId;
        this.date = date;
        this.requestCount = 0;
        this.successCount = 0;
        this.failureCount = 0;
        this.avgLatencyMs = 0;
        this.tokenExchanges = 0;
        this.userinfoRequests = 0;
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
    public UUID getProjectId() { return projectId; }
    public LocalDate getDate() { return date; }
    public long getRequestCount() { return requestCount; }
    public long getSuccessCount() { return successCount; }
    public long getFailureCount() { return failureCount; }
    public double getAvgLatencyMs() { return avgLatencyMs; }
    public OffsetDateTime getLastRequestAt() { return lastRequestAt; }
    public long getTokenExchanges() { return tokenExchanges; }
    public long getUserinfoRequests() { return userinfoRequests; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void incrementRequests() { this.requestCount++; }
    public void incrementSuccess() { this.successCount++; }
    public void incrementFailure() { this.failureCount++; }
    public void setAvgLatencyMs(double avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; }
    public void setLastRequestAt(OffsetDateTime lastRequestAt) { this.lastRequestAt = lastRequestAt; }
    public void incrementTokenExchanges() { this.tokenExchanges++; }
    public void incrementUserinfoRequests() { this.userinfoRequests++; }
}
