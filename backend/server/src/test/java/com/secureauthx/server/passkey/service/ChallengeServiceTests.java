package com.secureauthx.server.passkey.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.passkey.entity.WebAuthnChallenge;
import com.secureauthx.server.passkey.exception.WebAuthnException;
import com.secureauthx.server.passkey.repository.WebAuthnChallengeRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTests {

    @Mock
    private WebAuthnChallengeRepository challengeRepository;

    @Captor
    private ArgumentCaptor<WebAuthnChallenge> challengeCaptor;

    private ChallengeService challengeService;
    private User user;

    @BeforeEach
    void setUp() {
        challengeService = new ChallengeService(challengeRepository, 5);
        user = new User("test@example.com", "hash");
    }

    @Test
    void createChallengeGeneratesValidChallenge() {
        when(challengeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WebAuthnChallenge result = challengeService.createChallenge(user, "REGISTER");

        assertThat(result.getChallenge()).isNotNull().isNotBlank();
        assertThat(result.getPurpose()).isEqualTo("REGISTER");
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.isUsed()).isFalse();
        assertThat(result.isExpired()).isFalse();
    }

    @Test
    void consumeChallengeSucceedsForValidChallenge() {
        WebAuthnChallenge challenge = new WebAuthnChallenge("test-challenge", user, "REGISTER",
                OffsetDateTime.now().plusMinutes(5));
        when(challengeRepository.findByChallenge("test-challenge")).thenReturn(Optional.of(challenge));
        when(challengeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WebAuthnChallenge consumed = challengeService.consumeChallenge("test-challenge", "REGISTER");

        assertThat(consumed.isUsed()).isTrue();
    }

    @Test
    void consumeChallengeThrowsForUsedChallenge() {
        WebAuthnChallenge challenge = new WebAuthnChallenge("used-challenge", user, "REGISTER",
                OffsetDateTime.now().plusMinutes(5));
        challenge.markUsed();
        when(challengeRepository.findByChallenge("used-challenge")).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> challengeService.consumeChallenge("used-challenge", "REGISTER"))
                .isInstanceOf(WebAuthnException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    void consumeChallengeThrowsForExpiredChallenge() {
        WebAuthnChallenge challenge = new WebAuthnChallenge("expired-challenge", user, "REGISTER",
                OffsetDateTime.now().minusMinutes(1));
        when(challengeRepository.findByChallenge("expired-challenge")).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> challengeService.consumeChallenge("expired-challenge", "REGISTER"))
                .isInstanceOf(WebAuthnException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void consumeChallengeThrowsForWrongPurpose() {
        WebAuthnChallenge challenge = new WebAuthnChallenge("wrong-purpose", user, "AUTHENTICATE",
                OffsetDateTime.now().plusMinutes(5));
        when(challengeRepository.findByChallenge("wrong-purpose")).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> challengeService.consumeChallenge("wrong-purpose", "REGISTER"))
                .isInstanceOf(WebAuthnException.class)
                .hasMessageContaining("purpose mismatch");
    }
}
