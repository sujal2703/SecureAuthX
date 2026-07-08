package com.secureauthx.server.oauth.service;

import com.secureauthx.server.auth.entity.RefreshToken;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.jwt.JwtService;
import com.secureauthx.server.auth.repository.RefreshTokenRepository;
import com.secureauthx.server.oauth.dto.TokenResponse;
import com.secureauthx.server.oauth.entity.AuthorizationCode;
import com.secureauthx.server.oauth.entity.OAuthClient;
import com.secureauthx.server.oauth.exception.InvalidClientException;
import com.secureauthx.server.oauth.exception.UnauthorizedClientException;
import com.secureauthx.server.oauth.repository.OAuthClientRedirectUriRepository;
import com.secureauthx.server.sessions.service.SessionService;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OAuthAuthorizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthAuthorizationService.class);

    private final OAuthClientService oauthClientService;
    private final AuthorizationCodeService authorizationCodeService;
    private final PKCEService pkceService;
    private final OAuthClientRedirectUriRepository redirectUriRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SessionService sessionService;
    private final long refreshTokenExpirationDays;
    private final String oidcIssuer;

    public OAuthAuthorizationService(
            OAuthClientService oauthClientService,
            AuthorizationCodeService authorizationCodeService,
            PKCEService pkceService,
            OAuthClientRedirectUriRepository redirectUriRepository,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository,
            SessionService sessionService,
            @Value("${secureauthx.jwt.refresh-token-expiration-days:7}") long refreshTokenExpirationDays,
            @Value("${secureauthx.oidc.issuer:http://localhost:8080}") String oidcIssuer
    ) {
        this.oauthClientService = oauthClientService;
        this.authorizationCodeService = authorizationCodeService;
        this.pkceService = pkceService;
        this.redirectUriRepository = redirectUriRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.sessionService = sessionService;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.oidcIssuer = oidcIssuer;
    }

    public OAuthClient validateAuthorizationRequest(
            String clientId, String redirectUri, String responseType,
            String codeChallenge, String codeChallengeMethod
    ) {
        if (!"code".equals(responseType)) {
            throw new InvalidClientException("Unsupported response type: " + responseType);
        }

        OAuthClient client = oauthClientService.getOAuthClientByClientId(clientId);
        if (!client.isEnabled()) {
            throw new UnauthorizedClientException("Client is disabled.");
        }

        validateRedirectUri(client, redirectUri);

        if (!"S256".equals(codeChallengeMethod)) {
            throw new InvalidClientException("PKCE challenge method must be S256.");
        }

        if (codeChallenge == null || codeChallenge.isBlank()) {
            throw new InvalidClientException("PKCE code challenge is required.");
        }

        return client;
    }

    @Transactional
    public AuthorizationCode createAuthorizationCode(
            User user, OAuthClient client, String redirectUri,
            String codeChallenge, String codeChallengeMethod,
            String nonce, String scope
    ) {
        return authorizationCodeService.createAuthorizationCode(
                user, client, redirectUri, codeChallenge, codeChallengeMethod,
                nonce, scope
        );
    }

    @Transactional
    public TokenResponse handleAuthorizationCodeGrant(
            String code, String redirectUri, String clientId,
            String clientSecret, String codeVerifier
    ) {
        OAuthClient client = oauthClientService.authenticateClient(clientId, clientSecret);

        AuthorizationCode authCode = authorizationCodeService.consumeAuthorizationCode(code, redirectUri, client);

        pkceService.verify(authCode.getCodeChallenge(), authCode.getChallengeMethod(), codeVerifier);

        User user = authCode.getUser();

        String rawRefreshToken = generateRefreshTokenString();
        String tokenHash = hashToken(rawRefreshToken);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(refreshTokenExpirationDays);
        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        String ipAddress = "0.0.0.0";
        String userAgent = "OAuth Client";
        sessionService.createSession(user, refreshToken, ipAddress, userAgent);

        String accessToken = jwtService.createAccessToken(user.getId(), user.getEmail());
        long expiresIn = jwtService.getAccessTokenExpirationSeconds();

        String scope = authCode.getScope();
        boolean isOidc = scope != null && containsOpenIdScope(scope);

        String idToken = null;
        if (isOidc) {
            String nonce = authCode.getNonce();
            String clientIdValue = client.getClientId();
            Instant authTime = authCode.getCreatedAt().toInstant();
            idToken = jwtService.createIdToken(user.getId(), user.getEmail(), oidcIssuer, clientIdValue, nonce, authTime);
            LOGGER.info("ID Token issued for user_id={} client_id={}", user.getId(), clientIdValue);
        }

        LOGGER.info("Authorization code grant successful for user_id={} client_id={}", user.getId(), clientId);

        return new TokenResponse(accessToken, expiresIn, rawRefreshToken, scope, idToken);
    }

    private boolean containsOpenIdScope(String scope) {
        if (scope == null || scope.isBlank()) return false;
        String[] scopes = scope.split("\\s+");
        for (String s : scopes) {
            if ("openid".equals(s)) return true;
        }
        return false;
    }

    @Transactional
    public TokenResponse handleClientCredentialsGrant(String clientId, String clientSecret) {
        OAuthClient client = oauthClientService.authenticateClient(clientId, clientSecret);

        if (!client.isConfidential()) {
            throw new UnauthorizedClientException("Client credentials grant requires a confidential client.");
        }

        String accessToken = jwtService.createAccessToken(
                UUID.randomUUID(),
                client.getClientId()
        );
        long expiresIn = jwtService.getAccessTokenExpirationSeconds();

        LOGGER.info("Client credentials grant successful for client_id={}", clientId);

        return new TokenResponse(accessToken, expiresIn);
    }

    public void validateRedirectUri(OAuthClient client, String redirectUri) {
        List<String> allowedUris = redirectUriRepository.findByClientId(client.getId())
                .stream()
                .map(uri -> uri.getRedirectUri())
                .toList();

        if (allowedUris.isEmpty()) {
            throw new InvalidClientException("No redirect URIs registered for client.");
        }

        if (allowedUris.stream().noneMatch(uri -> uri.equals(redirectUri))) {
            throw new InvalidClientException("Redirect URI does not match any registered URI.");
        }
    }

    private String generateRefreshTokenString() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return HexFormat.of().withLowerCase().formatHex(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().withLowerCase().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash refresh token.", e);
        }
    }
}
