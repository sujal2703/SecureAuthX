package com.secureauthx.server.passkey.entity;

import com.secureauthx.server.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "passkeys")
public class Passkey {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "credential_id", nullable = false, unique = true, length = 1024)
    private String credentialId;

    @Column(name = "public_key", nullable = false, columnDefinition = "BYTEA")
    private byte[] publicKey;

    @Column(name = "counter", nullable = false)
    private long counter;

    @Column(name = "credential_type", nullable = false, length = 50)
    private String credentialType;

    @Column(name = "aaguid", length = 36)
    private String aaguid;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "backed_up", nullable = false)
    private boolean backedUp;

    @Column(name = "transports", length = 255)
    private String transports;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Passkey() {}

    public Passkey(User user, String credentialId, byte[] publicKey, String credentialType,
                   String aaguid, String deviceName, boolean backedUp, String transports) {
        this.user = user;
        this.credentialId = credentialId;
        this.publicKey = publicKey;
        this.counter = 0;
        this.credentialType = credentialType;
        this.aaguid = aaguid;
        this.deviceName = deviceName;
        this.backedUp = backedUp;
        this.transports = transports;
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

    public void updateCounter(long newCounter) {
        if (newCounter <= this.counter) {
            throw new IllegalStateException("Signature counter must increase monotonically.");
        }
        this.counter = newCounter;
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public String getCredentialId() { return credentialId; }
    public byte[] getPublicKey() { return publicKey; }
    public long getCounter() { return counter; }
    public String getCredentialType() { return credentialType; }
    public String getAaguid() { return aaguid; }
    public String getDeviceName() { return deviceName; }
    public boolean isBackedUp() { return backedUp; }
    public String getTransports() { return transports; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
