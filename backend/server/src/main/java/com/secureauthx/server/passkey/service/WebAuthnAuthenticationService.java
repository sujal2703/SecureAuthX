package com.secureauthx.server.passkey.service;

import com.secureauthx.server.auth.entity.RefreshToken;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.jwt.JwtService;
import com.secureauthx.server.auth.repository.RefreshTokenRepository;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.passkey.dto.AuthenticateOptionsResponse;
import com.secureauthx.server.passkey.dto.AuthenticateOptionsResponse.AllowCredential;
import com.secureauthx.server.passkey.dto.AuthenticateVerificationRequest;
import com.secureauthx.server.passkey.dto.AuthenticateVerificationResponse;
import com.secureauthx.server.passkey.dto.PasskeyResponse;
import com.secureauthx.server.passkey.entity.Passkey;
import com.secureauthx.server.passkey.entity.WebAuthnChallenge;
import com.secureauthx.server.passkey.exception.WebAuthnException;
import com.secureauthx.server.sessions.service.SessionService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebAuthnAuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebAuthnAuthenticationService.class);

    private final ChallengeService challengeService;
    private final PasskeyService passkeyService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SessionService sessionService;
    private final String rpId;
    private final String origin;
    private final long refreshTokenExpirationDays;

    public WebAuthnAuthenticationService(
            ChallengeService challengeService,
            PasskeyService passkeyService,
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository,
            SessionService sessionService,
            @Value("${secureauthx.webauthn.rp-id:localhost}") String rpId,
            @Value("${secureauthx.webauthn.origin:http://localhost:3000}") String origin,
            @Value("${secureauthx.jwt.refresh-token-expiration-days:7}") long refreshTokenExpirationDays
    ) {
        this.challengeService = challengeService;
        this.passkeyService = passkeyService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.sessionService = sessionService;
        this.rpId = rpId;
        this.origin = origin;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }

    public AuthenticateOptionsResponse generateAuthenticationOptions(String userHandle) {
        if (userHandle == null) {
            return AuthenticateOptionsResponse.forAuthenticate(
                    challengeService.createChallenge(null, "AUTHENTICATE").getChallenge(),
                    rpId,
                    List.of()
            );
        }

        UUID userId = UUID.fromString(userHandle);
        List<PasskeyResponse> passkeys = passkeyService.getUserPasskeys(userId);

        List<AllowCredential> allowCredentials = passkeys.stream()
                .map(pk -> new AllowCredential("public-key", pk.credentialId(), List.of()))
                .toList();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WebAuthnException("User not found."));

        return AuthenticateOptionsResponse.forAuthenticate(
                challengeService.createChallenge(user, "AUTHENTICATE").getChallenge(),
                rpId,
                allowCredentials
        );
    }

    @Transactional
    public AuthenticateVerificationResponse verifyAuthentication(
            AuthenticateVerificationRequest request, String ipAddress, String userAgent
    ) {
        String credentialId = request.id();
        Passkey passkey = passkeyService.getPasskeyByCredentialId(credentialId);
        User user = passkey.getUser();

        String clientDataJson = new String(
                Base64.getUrlDecoder().decode(request.clientDataJSON()),
                StandardCharsets.UTF_8
        );

        String challenge = extractChallenge(clientDataJson);
        WebAuthnChallenge storedChallenge = challengeService.consumeChallenge(challenge, "AUTHENTICATE");

        if (storedChallenge.getUser() != null && !storedChallenge.getUser().getId().equals(user.getId())) {
            throw new WebAuthnException("Challenge was issued for a different user.");
        }

        String actualOrigin = extractOrigin(clientDataJson);
        if (!origin.equals(actualOrigin)) {
            throw new WebAuthnException("Origin mismatch.");
        }

        String type = extractType(clientDataJson);
        if (!"webauthn.get".equals(type)) {
            throw new WebAuthnException("Invalid clientDataJSON type: " + type);
        }

        byte[] authenticatorData = Base64.getUrlDecoder().decode(request.authenticatorData());
        byte[] clientDataHash = sha256(
                request.clientDataJSON().getBytes(StandardCharsets.US_ASCII)
        );
        byte[] signature = Base64.getUrlDecoder().decode(request.signature());

        String rpIdHashHex = HexFormat.of().withLowerCase().formatHex(
                sha256(rpId.getBytes(StandardCharsets.UTF_8))
        );
        String actualRpIdHashHex = HexFormat.of().withLowerCase().formatHex(
                java.util.Arrays.copyOfRange(authenticatorData, 0, 32)
        );
        if (!rpIdHashHex.equals(actualRpIdHashHex)) {
            throw new WebAuthnException("RP ID hash mismatch.");
        }

        byte flags = authenticatorData[32];
        boolean userPresent = (flags & 0x01) != 0;
        boolean userVerified = (flags & 0x04) != 0;
        if (!userPresent || !userVerified) {
            throw new WebAuthnException("User verification required.");
        }

        long authCounter = 0;
        for (int i = 33; i < 37; i++) {
            authCounter = (authCounter << 8) | (authenticatorData[i] & 0xFF);
        }
        if (authCounter != 0) {
            passkey.updateCounter(authCounter);
        }

        byte[] signatureBase = concat(authenticatorData, clientDataHash);

        try {
            PublicKey publicKey = CoseKeyParser.parsePublicKey(passkey.getPublicKey());

            String algorithmName;
            if ("EC".equals(publicKey.getAlgorithm())) {
                algorithmName = "SHA256withECDSA";
            } else if ("RSA".equals(publicKey.getAlgorithm())) {
                algorithmName = "SHA256withRSA";
            } else {
                algorithmName = "SHA256withECDSA";
            }

            Signature sig = Signature.getInstance(algorithmName);
            sig.initVerify(publicKey);
            sig.update(signatureBase);
            if (!sig.verify(signature)) {
                return AuthenticateVerificationResponse.failed(credentialId);
            }
        } catch (WebAuthnException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Signature verification failed for credential_id={}", credentialId, e);
            return AuthenticateVerificationResponse.failed(credentialId);
        }

        String rawRefreshToken = generateRefreshTokenString();
        String tokenHash = hashToken(rawRefreshToken);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(refreshTokenExpirationDays);
        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        sessionService.createSession(user, refreshToken, ipAddress, userAgent);

        String accessToken = jwtService.createAccessToken(user.getId(), user.getEmail());

        LOGGER.info("Passkey authentication successful for user_id={} credential_id={}", user.getId(), credentialId);

        return AuthenticateVerificationResponse.verified(
                credentialId, accessToken, rawRefreshToken,
                jwtService.getAccessTokenExpirationSeconds()
        );
    }

    private String extractChallenge(String clientDataJson) {
        return extractJsonString(clientDataJson, "challenge");
    }

    private String extractOrigin(String clientDataJson) {
        return extractJsonString(clientDataJson, "origin");
    }

    private String extractType(String clientDataJson) {
        return extractJsonString(clientDataJson, "type");
    }

    private String extractJsonString(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) {
            throw new WebAuthnException("Missing field in clientDataJSON: " + key);
        }
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        if (end == -1) {
            throw new WebAuthnException("Malformed clientDataJSON for key: " + key);
        }
        return json.substring(start, end);
    }

    private byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available.", e);
        }
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private String generateRefreshTokenString() {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        return HexFormat.of().withLowerCase().formatHex(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withLowerCase().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash refresh token.", e);
        }
    }
}
