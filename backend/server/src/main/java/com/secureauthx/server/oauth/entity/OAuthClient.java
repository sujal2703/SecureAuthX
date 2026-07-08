package com.secureauthx.server.oauth.entity;

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
@Table(name = "oauth_clients")
public class OAuthClient {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "client_id", nullable = false, unique = true, length = 100)
    private String clientId;

    @Column(name = "client_secret", length = 255)
    private String hashedClientSecret;

    @Column(name = "client_name", nullable = false, length = 255)
    private String clientName;

    @Column(name = "confidential", nullable = false)
    private boolean confidential;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected OAuthClient() {}

    public OAuthClient(String clientId, String hashedClientSecret, String clientName, boolean confidential) {
        this.clientId = clientId;
        this.hashedClientSecret = hashedClientSecret;
        this.clientName = clientName;
        this.confidential = confidential;
        this.enabled = true;
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
    public String getClientId() { return clientId; }
    public String getHashedClientSecret() { return hashedClientSecret; }
    public String getClientName() { return clientName; }
    public boolean isConfidential() { return confidential; }
    public boolean isEnabled() { return enabled; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
