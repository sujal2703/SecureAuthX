package com.secureauthx.server.sessions.controller;

import com.secureauthx.server.sessions.dto.SessionResponse;
import com.secureauthx.server.sessions.exception.SessionNotFoundException;
import com.secureauthx.server.sessions.service.SessionService;
import com.secureauthx.server.common.exception.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    @Operation(
            summary = "List all active sessions",
            description = "Returns all active sessions for the authenticated user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of active sessions"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    ResponseEntity<List<SessionResponse>> listSessions(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UUID currentSessionId = (UUID) authentication.getCredentials();
        List<SessionResponse> sessions = sessionService.getActiveSessions(userId, currentSessionId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/current")
    @Operation(
            summary = "Get current session",
            description = "Returns details about the current session used to make this request.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current session details"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    ResponseEntity<SessionResponse> getCurrentSession(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UUID currentSessionId = (UUID) authentication.getCredentials();
        if (currentSessionId != null) {
            return ResponseEntity.ok(sessionService.getSessionResponse(currentSessionId, userId, currentSessionId));
        }
        List<SessionResponse> sessions = sessionService.getActiveSessions(userId, null);
        if (sessions.isEmpty()) {
            throw new SessionNotFoundException();
        }
        return ResponseEntity.ok(sessions.getFirst());
    }

    @DeleteMapping("/{sessionId}")
    @Operation(
            summary = "Revoke a session",
            description = "Revokes a specific session. Users may only revoke their own sessions.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Session revoked"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Session not found",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    ResponseEntity<Void> revokeSession(@PathVariable UUID sessionId, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        sessionService.revokeSessionById(sessionId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/current")
    @Operation(
            summary = "Revoke current session",
            description = "Revokes the current session. This is equivalent to logout from the current device.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Current session revoked"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    ResponseEntity<Void> revokeCurrentSession(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UUID currentSessionId = (UUID) authentication.getCredentials();
        if (currentSessionId != null) {
            sessionService.revokeSessionById(currentSessionId, userId);
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @Operation(
            summary = "Revoke all sessions",
            description = "Revokes every active session for the authenticated user. This logs out all devices.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "All sessions revoked"),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Authentication required",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    )
            }
    )
    ResponseEntity<Void> revokeAllSessions(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        sessionService.revokeAllSessions(userId);
        return ResponseEntity.noContent().build();
    }
}
