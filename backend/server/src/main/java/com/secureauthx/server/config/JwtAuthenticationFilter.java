package com.secureauthx.server.config;

import com.secureauthx.server.auth.jwt.JwtService;
import com.secureauthx.server.authorization.service.UserAuthorityService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserAuthorityService userAuthorityService;

    public JwtAuthenticationFilter(JwtService jwtService, UserAuthorityService userAuthorityService) {
        this.jwtService = jwtService;
        this.userAuthorityService = userAuthorityService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtService.validateToken(token);
                UUID userId = UUID.fromString(claims.getSubject());
                String sessionIdStr = claims.get("sessionId", String.class);
                UUID sessionId = sessionIdStr != null ? UUID.fromString(sessionIdStr) : null;
                Collection<GrantedAuthority> authorities = userAuthorityService.loadAuthorities(userId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, sessionId, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                LOGGER.debug("JWT authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
