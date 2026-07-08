package com.secureauthx.server.oauth.entity;

import com.secureauthx.server.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "oauth_authorization_codes")
public class AuthorizationCode {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 255)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private OAuthClient client;

    @Column(name = "redirect_uri", nullable = false, length = 2048)
    private String redirectUri;

    @Column(name = "code_challenge", nullable = false, length = 255)
    private String codeChallenge;

    @Column(name = "challenge_method", nullable = false, length = 10)
    private String challengeMethod;

    @Column(name = "nonce", length = 255)
    private String nonce;

    @Column(name = "scope", length = 1000)
    private String scope;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "consumed", nullable = false)
    private boolean consumed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected AuthorizationCode() {}

    public AuthorizationCode(
            String code, User user, OAuthClient client, String redirectUri,
            String codeChallenge, String challengeMethod, OffsetDateTime expiresAt,
            String nonce, String scope
    ) {
        this.code = code;
        this.user = user;
        this.client = client;
        this.redirectUri = redirectUri;
        this.codeChallenge = codeChallenge;
        this.challengeMethod = challengeMethod;
        this.expiresAt = expiresAt;
        this.nonce = nonce;
        this.scope = scope;
        this.consumed = false;
    }

    @PrePersist
    void onCreate() { createdAt = OffsetDateTime.now(); }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public User getUser() { return user; }
    public OAuthClient getClient() { return client; }
    public String getRedirectUri() { return redirectUri; }
    public String getCodeChallenge() { return codeChallenge; }
    public String getChallengeMethod() { return challengeMethod; }
    public String getNonce() { return nonce; }
    public String getScope() { return scope; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public boolean isConsumed() { return consumed; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void consume() { this.consumed = true; }
    public boolean isExpired() { return OffsetDateTime.now().isAfter(expiresAt); }
}
