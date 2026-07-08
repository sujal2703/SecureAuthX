package com.secureauthx.server.passkey.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.passkey.dto.PasskeyResponse;
import com.secureauthx.server.passkey.entity.Passkey;
import com.secureauthx.server.passkey.exception.PasskeyNotFoundException;
import com.secureauthx.server.passkey.repository.PasskeyRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasskeyServiceTests {

    @Mock
    private PasskeyRepository passkeyRepository;

    private PasskeyService passkeyService;
    private User user;
    private Passkey passkey;

    @BeforeEach
    void setUp() {
        passkeyService = new PasskeyService(passkeyRepository);
        user = new User("test@example.com", "hash");
        passkey = new Passkey(user, "cred-id-1", new byte[]{1, 2, 3}, "public-key",
                null, "Test Device", false, "usb");
    }

    @Test
    void savePasskeyStoresAndReturnsPasskey() {
        when(passkeyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Passkey result = passkeyService.savePasskey(passkey);

        assertThat(result.getCredentialId()).isEqualTo("cred-id-1");
        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    void getPasskeyByCredentialIdReturnsPasskey() {
        when(passkeyRepository.findByCredentialId("cred-id-1")).thenReturn(Optional.of(passkey));

        Passkey result = passkeyService.getPasskeyByCredentialId("cred-id-1");

        assertThat(result.getCredentialId()).isEqualTo("cred-id-1");
    }

    @Test
    void getPasskeyByCredentialIdThrowsForNotFound() {
        when(passkeyRepository.findByCredentialId("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passkeyService.getPasskeyByCredentialId("nonexistent"))
                .isInstanceOf(PasskeyNotFoundException.class);
    }

    @Test
    void getUserPasskeysReturnsList() {
        UUID userId = UUID.randomUUID();
        when(passkeyRepository.findByUserId(userId)).thenReturn(List.of(passkey));

        List<PasskeyResponse> result = passkeyService.getUserPasskeys(userId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().deviceName()).isEqualTo("Test Device");
    }

    @Test
    void deletePasskeyDeletesOwnedPasskey() {
        UUID passkeyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(passkeyRepository.findByIdAndUserId(passkeyId, userId)).thenReturn(Optional.of(passkey));

        passkeyService.deletePasskey(passkeyId, userId);

        verify(passkeyRepository).delete(passkey);
    }

    @Test
    void deletePasskeyThrowsForNotFound() {
        UUID passkeyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(passkeyRepository.findByIdAndUserId(passkeyId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passkeyService.deletePasskey(passkeyId, userId))
                .isInstanceOf(PasskeyNotFoundException.class);
    }
}
