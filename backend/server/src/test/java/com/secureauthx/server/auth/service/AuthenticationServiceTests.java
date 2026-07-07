package com.secureauthx.server.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    private AuthenticationService authenticationService;

    private static final String ACCESS_TOKEN = "access-token";
    private static final long EXPIRES_IN = 900;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository, refreshTokenRepository, passwordEncoder, jwtService, 7
        );
        lenient().when(jwtService.createAccessToken(any(UUID.class), anyString())).thenReturn(ACCESS_TOKEN);
        lenient().when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(EXPIRES_IN);
    }

    @Test
    void loginWithValidCredentialsReturnsTokens() {
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";
        String passwordHash = "$argon2id$hash";
        User user = new User(email, passwordHash);
        setField(user, "id", userId);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("S3cureExample!2026", passwordHash)).thenReturn(true);

        LoginRequest request = new LoginRequest("  User@Example.COM  ", "S3cureExample!2026");
        TokenResponse response = authenticationService.login(request);

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.expiresIn()).isEqualTo(EXPIRES_IN);
        assertThat(response.tokenType()).isEqualTo("Bearer");

        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        RefreshToken savedToken = refreshTokenCaptor.getValue();
        assertThat(savedToken.getUser().getId()).isEqualTo(userId);
        assertThat(savedToken.getTokenHash()).isNotBlank();
        assertThat(savedToken.getExpiresAt()).isAfter(OffsetDateTime.now());
    }

    @Test
    void loginWithInvalidEmailThrowsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("unknown@example.com", "S3cureExample!2026");
        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void loginWithInvalidPasswordThrowsException() {
        String email = "user@example.com";
        User user = new User(email, "$argon2id$hash");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.getPasswordHash())).thenReturn(false);

        LoginRequest request = new LoginRequest(email, "wrong-password");
        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshWithValidTokenReturnsNewTokens() {
        UUID userId = UUID.randomUUID();
        User user = new User("user@example.com", "$argon2id$hash");
        setField(user, "id", userId);

        OffsetDateTime futureExpiry = OffsetDateTime.now().plusDays(1);
        RefreshToken storedToken = new RefreshToken(user, "existing-hash", futureExpiry);
        setField(storedToken, "id", UUID.randomUUID());

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        TokenResponse response = authenticationService.refresh(request);

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotEqualTo("valid-refresh-token");
        assertThat(storedToken.isRevoked()).isTrue();
    }

    @Test
    void refreshWithRevokedTokenThrowsException() {
        UUID userId = UUID.randomUUID();
        User user = new User("user@example.com", "$argon2id$hash");
        setField(user, "id", userId);

        RefreshToken storedToken = new RefreshToken(user, "revoked-hash", OffsetDateTime.now().plusDays(1));
        setField(storedToken, "id", UUID.randomUUID());
        storedToken.revoke();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        RefreshTokenRequest request = new RefreshTokenRequest("revoked-token");
        assertThatThrownBy(() -> authenticationService.refresh(request))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshWithExpiredTokenThrowsException() {
        UUID userId = UUID.randomUUID();
        User user = new User("user@example.com", "$argon2id$hash");
        setField(user, "id", userId);

        RefreshToken storedToken = new RefreshToken(user, "expired-hash", OffsetDateTime.now().minusDays(1));
        setField(storedToken, "id", UUID.randomUUID());

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        RefreshTokenRequest request = new RefreshTokenRequest("expired-token");
        assertThatThrownBy(() -> authenticationService.refresh(request))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshWithNonExistentTokenThrowsException() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        RefreshTokenRequest request = new RefreshTokenRequest("nonexistent-token");
        assertThatThrownBy(() -> authenticationService.refresh(request))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void logoutRevokesToken() {
        UUID userId = UUID.randomUUID();
        User user = new User("user@example.com", "$argon2id$hash");
        setField(user, "id", userId);

        RefreshToken storedToken = new RefreshToken(user, "active-hash", OffsetDateTime.now().plusDays(1));
        setField(storedToken, "id", UUID.randomUUID());

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

        authenticationService.logout(new RefreshTokenRequest("active-token"));

        assertThat(storedToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(storedToken);
    }

    @Test
    void logoutWithNonExistentTokenSucceedsSilently() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        authenticationService.logout(new RefreshTokenRequest("unknown-token"));

        verify(refreshTokenRepository, never()).save(any());
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
