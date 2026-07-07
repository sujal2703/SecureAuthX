package com.secureauthx.server.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTests {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(15);
    }

    @Test
    void createsAndValidatesAccessToken() {
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";

        String token = jwtService.createAccessToken(userId, email);
        assertThat(token).isNotBlank();

        Claims claims = jwtService.validateToken(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo(email);
    }

    @Test
    void rejectsInvalidToken() {
        assertThatThrownBy(() -> jwtService.validateToken("invalid-token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void returnsAccessTokenExpirationSeconds() {
        assertThat(jwtService.getAccessTokenExpirationSeconds()).isEqualTo(900);
    }

    @Test
    void createsTokenWithCorrectExpirationForDifferentDuration() {
        JwtService shortLived = new JwtService(1);
        assertThat(shortLived.getAccessTokenExpirationSeconds()).isEqualTo(60);
    }
}
