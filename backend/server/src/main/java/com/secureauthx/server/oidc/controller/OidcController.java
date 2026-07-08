package com.secureauthx.server.oidc.controller;

import com.secureauthx.server.auth.entity.User;
import com.secureauthx.server.auth.jwt.JwtService;
import com.secureauthx.server.auth.repository.UserRepository;
import com.secureauthx.server.oidc.dto.UserInfoResponse;
import com.secureauthx.server.oidc.service.OidcService;
import io.jsonwebtoken.Claims;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OidcController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcController.class);

    private final OidcService oidcService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public OidcController(OidcService oidcService, JwtService jwtService, UserRepository userRepository) {
        this.oidcService = oidcService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @GetMapping("/.well-known/openid-configuration")
    public ResponseEntity<Map<String, Object>> openidConfiguration() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(oidcService.getDiscoveryDocument());
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(oidcService.getJwks());
    }

    @GetMapping("/connect/userinfo")
    public ResponseEntity<?> userinfo(
            @org.springframework.web.bind.annotation.RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.validateToken(token);

            String subject = claims.getSubject();
            if (subject == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = userRepository.findById(UUID.fromString(subject))
                    .orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserInfoResponse userInfo = oidcService.buildUserInfo(user);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            LOGGER.warn("UserInfo request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
