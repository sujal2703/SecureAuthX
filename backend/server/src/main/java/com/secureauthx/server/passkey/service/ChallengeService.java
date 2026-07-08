package com.secureauthx.server.passkey.service;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.passkey.entity.WebAuthnChallenge;
import com.secureauthx.server.passkey.exception.WebAuthnException;
import com.secureauthx.server.passkey.repository.WebAuthnChallengeRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChallengeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChallengeService.class);

    private final WebAuthnChallengeRepository challengeRepository;
    private final long challengeExpirationMinutes;

    public ChallengeService(
            WebAuthnChallengeRepository challengeRepository,
            @Value("${secureauthx.webauthn.challenge-expiration-minutes:5}") long challengeExpirationMinutes
    ) {
        this.challengeRepository = challengeRepository;
        this.challengeExpirationMinutes = challengeExpirationMinutes;
    }

    @Transactional
    public WebAuthnChallenge createChallenge(User user, String purpose) {
        String challengeValue = generateChallenge();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(challengeExpirationMinutes);
        WebAuthnChallenge challenge = new WebAuthnChallenge(challengeValue, user, purpose, expiresAt);
        WebAuthnChallenge saved = challengeRepository.save(challenge);
        if (user != null) {
            LOGGER.debug("WebAuthn challenge created for user_id={} purpose={}", user.getId(), purpose);
        } else {
            LOGGER.debug("WebAuthn challenge created for anonymous user purpose={}", purpose);
        }
        return saved;
    }

    @Transactional
    public WebAuthnChallenge consumeChallenge(String challengeValue, String expectedPurpose) {
        WebAuthnChallenge challenge = challengeRepository.findByChallenge(challengeValue)
                .orElseThrow(() -> new WebAuthnException("Challenge not found."));

        if (challenge.isUsed()) {
            throw new WebAuthnException("Challenge has already been used.");
        }

        if (challenge.isExpired()) {
            throw new WebAuthnException("Challenge has expired.");
        }

        if (!challenge.getPurpose().equals(expectedPurpose)) {
            throw new WebAuthnException("Challenge purpose mismatch.");
        }

        challenge.markUsed();
        challengeRepository.save(challenge);

        LOGGER.debug("WebAuthn challenge consumed purpose={}", expectedPurpose);
        return challenge;
    }

    private String generateChallenge() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
