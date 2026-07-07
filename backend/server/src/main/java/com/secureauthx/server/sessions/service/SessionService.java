package com.secureauthx.server.sessions.service;

import com.secureauthx.server.auth.entity.RefreshToken;
import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.repository.RefreshTokenRepository;
import com.secureauthx.server.sessions.dto.SessionResponse;
import com.secureauthx.server.sessions.entity.Session;
import com.secureauthx.server.sessions.exception.SessionNotFoundException;
import com.secureauthx.server.sessions.repository.SessionRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAgentParser userAgentParser;

    public SessionService(
            SessionRepository sessionRepository,
            RefreshTokenRepository refreshTokenRepository,
            UserAgentParser userAgentParser
    ) {
        this.sessionRepository = sessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAgentParser = userAgentParser;
    }

    @Transactional
    public Session createSession(User user, RefreshToken refreshToken, String ipAddress, String userAgent) {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(7);
        Session session = new Session(
                user,
                refreshToken,
                expiresAt,
                ipAddress,
                userAgent,
                userAgentParser.parseDeviceName(userAgent),
                userAgentParser.parseOperatingSystem(userAgent),
                userAgentParser.parseBrowser(userAgent)
        );
        Session saved = sessionRepository.save(session);
        LOGGER.info("Session created for user_id={} session_id={}", user.getId(), saved.getId());
        return saved;
    }

    @Transactional
    public void updateSessionRefreshToken(Session session, RefreshToken newRefreshToken) {
        session.updateRefreshToken(newRefreshToken);
        sessionRepository.save(session);
    }

    @Transactional
    public void updateActivity(Session session) {
        sessionRepository.save(session);
    }

    @Transactional
    public void revokeSession(Session session) {
        session.revoke();
        sessionRepository.save(session);
    }

    public List<SessionResponse> getActiveSessions(UUID userId, UUID currentSessionId) {
        List<Session> sessions = sessionRepository
                .findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastActivityAtDesc(
                        userId, OffsetDateTime.now());
        return sessions.stream()
                .map(s -> SessionResponse.from(s, currentSessionId))
                .toList();
    }

    public SessionResponse getSessionResponse(UUID sessionId, UUID userId, UUID currentSessionId) {
        Session session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(SessionNotFoundException::new);
        return SessionResponse.from(session, currentSessionId);
    }

    public Session getSessionByIdAndUser(UUID sessionId, UUID userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(SessionNotFoundException::new);
    }

    @Transactional
    public void revokeSessionById(UUID sessionId, UUID userId) {
        Session session = getSessionByIdAndUser(sessionId, userId);
        session.revoke();
        sessionRepository.save(session);
        if (session.getRefreshToken() != null) {
            session.getRefreshToken().revoke();
            refreshTokenRepository.save(session.getRefreshToken());
        }
        LOGGER.info("Session revoked session_id={} user_id={}", sessionId, userId);
    }

    @Transactional
    public void revokeAllSessions(UUID userId) {
        List<Session> activeSessions = sessionRepository
                .findByUserIdAndRevokedAtIsNullAndExpiresAtAfterOrderByLastActivityAtDesc(
                        userId, OffsetDateTime.now());
        for (Session session : activeSessions) {
            session.revoke();
            if (session.getRefreshToken() != null) {
                session.getRefreshToken().revoke();
                refreshTokenRepository.save(session.getRefreshToken());
            }
        }
        sessionRepository.saveAll(activeSessions);
        LOGGER.info("All sessions revoked for user_id={} count={}", userId, activeSessions.size());
    }
}
