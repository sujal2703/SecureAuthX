package com.secureauthx.server.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final Set<String> RATE_LIMITED_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh"
    );

    private final RateLimitingService rateLimitingService;

    public RateLimitingFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!RATE_LIMITED_PATHS.contains(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        int limit;
        String path = request.getRequestURI();

        if (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register")) {
            limit = 10;
        } else if (path.equals("/api/v1/auth/refresh")) {
            limit = 20;
        } else {
            limit = 60;
        }

        boolean allowed;
        UUID userId = extractUserId();
        if (userId != null) {
            allowed = rateLimitingService.isAllowed(userId, limit, Duration.ofMinutes(1));
        } else {
            String ip = request.getRemoteAddr();
            allowed = rateLimitingService.isAllowedByIp(ip, limit, Duration.ofMinutes(1));
        }

        if (!allowed) {
            LOGGER.warn("Rate limit exceeded for path={}", path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            try (var writer = response.getWriter()) {
                writer.write(
                        "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    private UUID extractUserId() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UUID userId) {
                return userId;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
