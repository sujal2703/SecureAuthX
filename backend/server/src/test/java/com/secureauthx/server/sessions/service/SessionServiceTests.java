package com.secureauthx.server.sessions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.secureauthx.server.auth.entity.RefreshToken;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.RefreshTokenRepository;
import com.secureauthx.server.sessions.dto.SessionResponse;
import com.secureauthx.server.sessions.entity.Session;
import com.secureauthx.server.sessions.exception.SessionNotFoundException;
import com.secureauthx.server.sessions.repository.SessionRepository;
import java.time.OffsetDateTime;
import java.util.List;
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
class SessionServiceTests {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserAgentParser userAgentParser;

    @Captor
    private ArgumentCaptor<Session> sessionCaptor;

    private SessionService sessionService;

    private UUID userId;
    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(sessionRepository, refreshTokenRepository, userAgentParser);
        userId = UUID.randomUUID();
        user = new User("user@example.com", "$argon2id$hash");
        setField(user, "id", userId);
        refreshToken = new RefreshToken(user, "token-hash", OffsetDateTime.now().plusDays(7));
        setField(refreshToken, "id", UUID.randomUUID());

        lenient().when(userAgentParser.parseBrowser(any())).thenReturn("Chrome");
        lenient().when(userAgentParser.parseOperatingSystem(any())).thenReturn("Windows");
        lenient().when(userAgentParser.parseDeviceName(any())).thenReturn("Windows PC");
    }

    @Test
    void createsSessionWithDeviceInfo() {
        when(sessionRepository.save(any())).thenAnswer(invocation -> {
            Session session = invocation.getArgument(0);
            setField(session, "id", UUID.randomUUID());
            return session;
        });

        Session session = sessionService.createSession(user, refreshToken, "192.168.1.1", "Mozilla/5.0 Chrome/120");

        assertThat(session).isNotNull();
        assertThat(session.getUser().getId()).isEqualTo(userId);
        assertThat(session.getRefreshToken().getId()).isEqualTo(refreshToken.getId());
        assertThat(session.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(session.getBrowser()).isEqualTo("Chrome");
        assertThat(session.getOperatingSystem()).isEqualTo("Windows");
        assertThat(session.getDeviceName()).isEqualTo("Windows PC");
        assertThat(session.isRevoked()).isFalse();
        assertThat(session.getExpiresAt()).isAfter(OffsetDateTime.now());
    }

    @Test
    void listsActiveSessions() {
        UUID sessionId1 = UUID.randomUUID();
        UUID sessionId2 = UUID.randomUUID();

        Session session1 = createSession(user, refreshToken);
        setField(session1, "id", sessionId1);
        Session session2 = createSession(user, refreshToken);
        setField(session2, "id", sessionId2);

        when(sessionRepository.findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastActivityAtDesc(
                any(), any())).thenReturn(List.of(session1, session2));

        List<SessionResponse> sessions = sessionService.getActiveSessions(userId, sessionId1);

        assertThat(sessions).hasSize(2);
        assertThat(sessions.getFirst().isCurrent()).isTrue();
        assertThat(sessions.getLast().isCurrent()).isFalse();
    }

    @Test
    void revokesSessionAndItsRefreshToken() {
        UUID sessionId = UUID.randomUUID();
        Session session = createSession(user, refreshToken);
        setField(session, "id", sessionId);

        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        sessionService.revokeSessionById(sessionId, userId);

        assertThat(session.isRevoked()).isTrue();
        verify(sessionRepository).save(session);
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void revokeSessionThrowsForWrongUser() {
        UUID sessionId = UUID.randomUUID();
        UUID wrongUserId = UUID.randomUUID();
        Session session = createSession(user, refreshToken);
        setField(session, "id", sessionId);

        when(sessionRepository.findByIdAndUserId(sessionId, wrongUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.revokeSessionById(sessionId, wrongUserId))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void revokesAllSessions() {
        UUID sessionId1 = UUID.randomUUID();
        UUID sessionId2 = UUID.randomUUID();

        Session session1 = createSession(user, refreshToken);
        setField(session1, "id", sessionId1);
        Session session2 = createSession(user, refreshToken);
        setField(session2, "id", sessionId2);

        when(sessionRepository.findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastActivityAtDesc(
                any(), any())).thenReturn(List.of(session1, session2));

        sessionService.revokeAllSessions(userId);

        assertThat(session1.isRevoked()).isTrue();
        assertThat(session2.isRevoked()).isTrue();
        verify(sessionRepository).saveAll(any());
    }

    private Session createSession(User user, RefreshToken refreshToken) {
        return new Session(user, refreshToken, OffsetDateTime.now().plusDays(7),
                "192.168.1.1", "Mozilla/5.0", "Windows PC", "Windows", "Chrome");
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
