package com.secureauthx.server.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessTokenExpirationMinutes;

    public JwtService(
            @Value("${secureauthx.jwt.access-token-expiration-minutes:15}") long accessTokenExpirationMinutes
    ) {
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        KeyPair keyPair = loadOrGenerateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        LOGGER.info("JWT key pair loaded. Access tokens expire after {} minutes.", accessTokenExpirationMinutes);
    }

    public String createAccessToken(UUID userId, String email) {
        return createAccessToken(userId, email, null);
    }

    public String createAccessToken(UUID userId, String email, UUID sessionId) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(accessTokenExpirationMinutes))))
                .signWith(privateKey, Jwts.SIG.RS256);
        if (sessionId != null) {
            builder.claim("sessionId", sessionId.toString());
        }
        return builder.compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid or expired token.", e);
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return Duration.ofMinutes(accessTokenExpirationMinutes).toSeconds();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String createIdToken(UUID userId, String email, String issuer, String audience, String nonce, Instant authTime) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .signWith(privateKey, Jwts.SIG.RS256);
        if (email != null) {
            builder.claim("email", email);
        }
        if (nonce != null) {
            builder.claim("nonce", nonce);
        }
        if (authTime != null) {
            builder.claim("auth_time", authTime.getEpochSecond());
        }
        return builder.compact();
    }

    private KeyPair loadOrGenerateKeyPair() {
        try {
            String privateKeyB64 = System.getenv("SECUREAUTHX_JWT_PRIVATE_KEY");
            String publicKeyB64 = System.getenv("SECUREAUTHX_JWT_PUBLIC_KEY");

            if (privateKeyB64 != null && publicKeyB64 != null && !privateKeyB64.isBlank() && !publicKeyB64.isBlank()) {
                LOGGER.info("Loading JWT key pair from environment variables.");
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PrivateKey privateKey = keyFactory.generatePrivate(
                        new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyB64)));
                PublicKey publicKey = keyFactory.generatePublic(
                        new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyB64)));
                return new KeyPair(publicKey, privateKey);
            }

            LOGGER.warn("SECUREAUTHX_JWT_PRIVATE_KEY and SECUREAUTHX_JWT_PUBLIC_KEY not set. Generating ephemeral RSA key pair. "
                    + "Tokens signed with this key will be invalid after restart. "
                    + "Set both environment variables for production deployments.");
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWT key pair.", e);
        }
    }
}
