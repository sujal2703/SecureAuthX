package com.secureauthx.server.oauth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.oauth.entity.AuthorizationCode;
import com.secureauthx.server.oauth.entity.OAuthClient;
import com.secureauthx.server.oauth.exception.InvalidGrantException;
import com.secureauthx.server.oauth.repository.AuthorizationCodeRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorizationCodeServiceTests {

    @Mock
    private AuthorizationCodeRepository authorizationCodeRepository;

    private AuthorizationCodeService authorizationCodeService;

    private User user;
    private OAuthClient client;
    private String redirectUri;
    private String codeChallenge;
    private String challengeMethod;

    @BeforeEach
    void setUp() {
        authorizationCodeService = new AuthorizationCodeService(authorizationCodeRepository);

        user = new User("user@example.com", "$argon2id$hash");
        setField(user, "id", UUID.randomUUID());

        client = new OAuthClient("my-client", null, "My Client", false);
        setField(client, "id", UUID.randomUUID());

        redirectUri = "https://example.com/callback";
        codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";
        challengeMethod = "S256";
    }

    @Test
    void createsAuthorizationCode() {
        when(authorizationCodeRepository.save(any(AuthorizationCode.class))).thenAnswer(inv -> {
            AuthorizationCode ac = inv.getArgument(0);
            setField(ac, "id", UUID.randomUUID());
            return ac;
        });

        AuthorizationCode authCode = authorizationCodeService.createAuthorizationCode(
                user, client, redirectUri, codeChallenge, challengeMethod, null, null
        );

        assertThat(authCode.getCode()).isNotBlank();
        assertThat(authCode.getUser().getId()).isEqualTo(user.getId());
        assertThat(authCode.getClient().getId()).isEqualTo(client.getId());
        assertThat(authCode.getRedirectUri()).isEqualTo(redirectUri);
        assertThat(authCode.getCodeChallenge()).isEqualTo(codeChallenge);
        assertThat(authCode.getExpiresAt()).isAfter(OffsetDateTime.now());
        assertThat(authCode.isConsumed()).isFalse();
    }

    @Test
    void consumesValidAuthorizationCode() {
        AuthorizationCode authCode = new AuthorizationCode(
                "test-code", user, client, redirectUri,
                codeChallenge, challengeMethod, OffsetDateTime.now().plusMinutes(5),
                null, null
        );
        setField(authCode, "id", UUID.randomUUID());

        when(authorizationCodeRepository.findByCode("test-code")).thenReturn(Optional.of(authCode));
        when(authorizationCodeRepository.save(any(AuthorizationCode.class))).thenReturn(authCode);

        AuthorizationCode result = authorizationCodeService.consumeAuthorizationCode(
                "test-code", redirectUri, client
        );

        assertThat(result.isConsumed()).isTrue();
    }

    @Test
    void rejectsAlreadyConsumedCode() {
        AuthorizationCode authCode = new AuthorizationCode(
                "used-code", user, client, redirectUri,
                codeChallenge, challengeMethod, OffsetDateTime.now().plusMinutes(5),
                null, null
        );
        setField(authCode, "id", UUID.randomUUID());
        authCode.consume();

        when(authorizationCodeRepository.findByCode("used-code")).thenReturn(Optional.of(authCode));

        assertThatThrownBy(() -> authorizationCodeService.consumeAuthorizationCode(
                "used-code", redirectUri, client
        )).isInstanceOf(InvalidGrantException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    void rejectsExpiredCode() {
        AuthorizationCode authCode = new AuthorizationCode(
                "expired-code", user, client, redirectUri,
                codeChallenge, challengeMethod, OffsetDateTime.now().minusMinutes(1),
                null, null
        );
        setField(authCode, "id", UUID.randomUUID());

        when(authorizationCodeRepository.findByCode("expired-code")).thenReturn(Optional.of(authCode));

        assertThatThrownBy(() -> authorizationCodeService.consumeAuthorizationCode(
                "expired-code", redirectUri, client
        )).isInstanceOf(InvalidGrantException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void rejectsCodeForWrongClient() {
        OAuthClient differentClient = new OAuthClient("other-client", null, "Other", false);
        setField(differentClient, "id", UUID.randomUUID());

        AuthorizationCode authCode = new AuthorizationCode(
                "code", user, differentClient, redirectUri,
                codeChallenge, challengeMethod, OffsetDateTime.now().plusMinutes(5),
                null, null
        );
        setField(authCode, "id", UUID.randomUUID());

        when(authorizationCodeRepository.findByCode("code")).thenReturn(Optional.of(authCode));

        assertThatThrownBy(() -> authorizationCodeService.consumeAuthorizationCode(
                "code", redirectUri, client
        )).isInstanceOf(InvalidGrantException.class)
                .hasMessageContaining("different client");
    }

    @Test
    void rejectsRedirectUriMismatch() {
        AuthorizationCode authCode = new AuthorizationCode(
                "code", user, client, "https://original.com/callback",
                codeChallenge, challengeMethod, OffsetDateTime.now().plusMinutes(5),
                null, null
        );
        setField(authCode, "id", UUID.randomUUID());

        when(authorizationCodeRepository.findByCode("code")).thenReturn(Optional.of(authCode));

        assertThatThrownBy(() -> authorizationCodeService.consumeAuthorizationCode(
                "code", "https://different.com/callback", client
        )).isInstanceOf(InvalidGrantException.class)
                .hasMessageContaining("Redirect URI mismatch");
    }

    @Test
    void rejectsNonExistentCode() {
        when(authorizationCodeRepository.findByCode("unknown-code")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorizationCodeService.consumeAuthorizationCode(
                "unknown-code", redirectUri, client
        )).isInstanceOf(InvalidGrantException.class)
                .hasMessageContaining("not found");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
