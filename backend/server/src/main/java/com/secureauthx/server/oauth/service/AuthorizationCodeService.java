package com.secureauthx.server.oauth.service;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.oauth.entity.AuthorizationCode;
import com.secureauthx.server.oauth.entity.OAuthClient;
import com.secureauthx.server.oauth.exception.InvalidGrantException;
import com.secureauthx.server.oauth.repository.AuthorizationCodeRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorizationCodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationCodeService.class);
    private static final long CODE_EXPIRATION_MINUTES = 10;

    private final AuthorizationCodeRepository authorizationCodeRepository;

    public AuthorizationCodeService(AuthorizationCodeRepository authorizationCodeRepository) {
        this.authorizationCodeRepository = authorizationCodeRepository;
    }

    @Transactional
    public AuthorizationCode createAuthorizationCode(
            User user, OAuthClient client, String redirectUri,
            String codeChallenge, String challengeMethod
    ) {
        String code = generateAuthorizationCode();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);

        AuthorizationCode authCode = new AuthorizationCode(
                code, user, client, redirectUri,
                codeChallenge, challengeMethod, expiresAt
        );
        AuthorizationCode saved = authorizationCodeRepository.save(authCode);

        LOGGER.info("Authorization code created for user_id={} client_id={}", user.getId(), client.getClientId());

        return saved;
    }

    @Transactional
    public AuthorizationCode consumeAuthorizationCode(String code, String redirectUri, OAuthClient client) {
        AuthorizationCode authCode = authorizationCodeRepository.findByCode(code)
                .orElseThrow(() -> new InvalidGrantException("Authorization code not found."));

        if (authCode.isConsumed()) {
            throw new InvalidGrantException("Authorization code has already been used.");
        }

        if (authCode.isExpired()) {
            throw new InvalidGrantException("Authorization code has expired.");
        }

        if (!authCode.getClient().getId().equals(client.getId())) {
            throw new InvalidGrantException("Authorization code was issued for a different client.");
        }

        if (!authCode.getRedirectUri().equals(redirectUri)) {
            throw new InvalidGrantException("Redirect URI mismatch.");
        }

        authCode.consume();
        authorizationCodeRepository.save(authCode);

        LOGGER.info("Authorization code consumed for user_id={}", authCode.getUser().getId());

        return authCode;
    }

    private String generateAuthorizationCode() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
