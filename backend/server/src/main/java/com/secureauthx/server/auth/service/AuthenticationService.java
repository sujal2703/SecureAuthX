package com.secureauthx.server.auth.service;

import com.secureauthx.server.auth.dto.LoginRequest;
import com.secureauthx.server.auth.dto.RefreshTokenRequest;
import com.secureauthx.server.auth.dto.TokenResponse;
import com.secureauthx.server.auth.entity.RefreshToken;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.exception.InvalidCredentialsException;
import com.secureauthx.server.auth.exception.InvalidTokenException;
import com.secureauthx.server.auth.jwt.JwtService;
import com.secureauthx.server.auth.repository.RefreshTokenRepository;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.sessions.entity.Session;
import com.secureauthx.server.sessions.service.SessionService;
import java.security.MessageDigest;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final long refreshTokenExpirationDays;

    public AuthenticationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            SessionService sessionService,
            @Value("${secureauthx.jwt.refresh-token-expiration-days:7}") long refreshTokenExpirationDays
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String normalizedEmail = normalizeEmail(request.email());
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String rawRefreshToken = generateRefreshTokenString();
        String tokenHash = hashToken(rawRefreshToken);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(refreshTokenExpirationDays);
        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        Session session = sessionService.createSession(user, refreshToken, ipAddress, userAgent);

        String accessToken = jwtService.createAccessToken(user.getId(), user.getEmail(), session.getId());

        LOGGER.info("User login successful for user_id={}", user.getId());

        return new TokenResponse(
                accessToken,
                rawRefreshToken,
                jwtService.getAccessTokenExpirationSeconds(),
                "Bearer"
        );
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request, String ipAddress, String userAgent) {
        String tokenHash = hashToken(request.refreshToken());
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found."));

        if (storedToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked.");
        }

        if (storedToken.isExpired()) {
            throw new InvalidTokenException("Refresh token has expired.");
        }

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        String rawRefreshToken = generateRefreshTokenString();
        String newTokenHash = hashToken(rawRefreshToken);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(refreshTokenExpirationDays);
        RefreshToken newRefreshToken = new RefreshToken(storedToken.getUser(), newTokenHash, expiresAt);
        refreshTokenRepository.save(newRefreshToken);

        Session session = sessionService.createSession(storedToken.getUser(), newRefreshToken, ipAddress, userAgent);

        String accessToken = jwtService.createAccessToken(
                storedToken.getUser().getId(),
                storedToken.getUser().getEmail(),
                session.getId()
        );

        LOGGER.info("Token refresh successful for user_id={}", storedToken.getUser().getId());

        return new TokenResponse(
                accessToken,
                rawRefreshToken,
                jwtService.getAccessTokenExpirationSeconds(),
                "Bearer"
        );
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.refreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(storedToken -> {
            storedToken.revoke();
            refreshTokenRepository.save(storedToken);
            LOGGER.info("Logout successful for user_id={}", storedToken.getUser().getId());
        });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateRefreshTokenString() {
        byte[] randomBytes = new byte[64];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);
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
