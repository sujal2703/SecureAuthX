package com.secureauthx.server.passkey.service;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.passkey.dto.RegisterOptionsResponse;
import com.secureauthx.server.passkey.dto.RegisterVerificationRequest;
import com.secureauthx.server.passkey.dto.RegisterVerificationResponse;
import com.secureauthx.server.passkey.entity.Passkey;
import com.secureauthx.server.passkey.entity.WebAuthnChallenge;
import com.secureauthx.server.passkey.exception.WebAuthnException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebAuthnRegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebAuthnRegistrationService.class);

    private final ChallengeService challengeService;
    private final PasskeyService passkeyService;
    private final String rpId;
    private final String rpName;
    private final String origin;

    public WebAuthnRegistrationService(
            ChallengeService challengeService,
            PasskeyService passkeyService,
            @Value("${secureauthx.webauthn.rp-id:localhost}") String rpId,
            @Value("${secureauthx.webauthn.rp-name:SecureAuthX}") String rpName,
            @Value("${secureauthx.webauthn.origin:http://localhost:3000}") String origin
    ) {
        this.challengeService = challengeService;
        this.passkeyService = passkeyService;
        this.rpId = rpId;
        this.rpName = rpName;
        this.origin = origin;
    }

    @Transactional
    public RegisterOptionsResponse generateRegistrationOptions(User user) {
        WebAuthnChallenge challenge = challengeService.createChallenge(user, "REGISTER");
        return RegisterOptionsResponse.forRegister(
                challenge.getChallenge(),
                rpId,
                rpName,
                user.getId().toString(),
                user.getEmail()
        );
    }

    @Transactional
    public RegisterVerificationResponse verifyRegistration(User user, RegisterVerificationRequest request) {
        String clientDataJson = new String(
                Base64.getUrlDecoder().decode(request.clientDataJSON()),
                StandardCharsets.UTF_8
        );

        String challenge = extractChallenge(clientDataJson);
        WebAuthnChallenge storedChallenge = challengeService.consumeChallenge(challenge, "REGISTER");

        if (!storedChallenge.getUser().getId().equals(user.getId())) {
            throw new WebAuthnException("Challenge was issued for a different user.");
        }

        String actualOrigin = extractOrigin(clientDataJson);
        if (!origin.equals(actualOrigin)) {
            throw new WebAuthnException("Origin mismatch. Expected: " + origin + ", got: " + actualOrigin);
        }

        String type = extractType(clientDataJson);
        if (!"webauthn.create".equals(type)) {
            throw new WebAuthnException("Invalid clientDataJSON type: " + type);
        }

        byte[] publicKeyBytes = Base64.getUrlDecoder().decode(request.publicKey());

        String credentialId = request.id();

        if (passkeyService.getUserPasskeys(user.getId()).stream()
                .anyMatch(p -> p.credentialId().equals(credentialId))) {
            throw new WebAuthnException("Credential ID already registered.");
        }

        Integer algorithm = request.publicKeyAlgorithm() != null
                ? Integer.parseInt(request.publicKeyAlgorithm())
                : -7;

        String credentialType = request.type() != null ? request.type() : "public-key";
        String aaguid = request.aaguid();
        String deviceName = request.deviceName();
        boolean backedUp = false;
        String transports = request.transports();

        Passkey passkey = new Passkey(
                user, credentialId, publicKeyBytes, credentialType,
                aaguid, deviceName, backedUp, transports
        );
        passkeyService.savePasskey(passkey);

        LOGGER.info("Passkey registered for user_id={} credential_id={}", user.getId(), credentialId);

        return new RegisterVerificationResponse(passkey.getId(), true, credentialId);
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
}
