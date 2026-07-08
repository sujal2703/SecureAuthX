package com.secureauthx.server.oauth.service;

import com.secureauthx.server.oauth.exception.InvalidGrantException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class PKCEService {

    private static final String S256 = "S256";

    public void verify(String codeChallenge, String challengeMethod, String codeVerifier) {
        if (challengeMethod == null || !S256.equals(challengeMethod)) {
            throw new InvalidGrantException("PKCE challenge method must be S256.");
        }

        if (codeVerifier == null || codeVerifier.isBlank()) {
            throw new InvalidGrantException("PKCE code verifier is required.");
        }

        if (codeVerifier.length() < 43 || codeVerifier.length() > 128) {
            throw new InvalidGrantException("PKCE code verifier must be between 43 and 128 characters.");
        }

        String computedChallenge = computeS256Challenge(codeVerifier);

        if (!MessageDigest.isEqual(
                computedChallenge.getBytes(StandardCharsets.US_ASCII),
                codeChallenge.getBytes(StandardCharsets.US_ASCII)
        )) {
            throw new InvalidGrantException("PKCE code verifier does not match code challenge.");
        }
    }

    public String computeS256Challenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute S256 challenge.", e);
        }
    }

    public String generateCodeVerifier() {
        byte[] codeVerifier = new byte[32];
        new SecureRandom().nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    public String generateCodeChallenge(String codeVerifier) {
        return computeS256Challenge(codeVerifier);
    }
}
