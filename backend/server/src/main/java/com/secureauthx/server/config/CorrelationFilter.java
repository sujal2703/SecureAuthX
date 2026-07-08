package com.secureauthx.server.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String EXECUTION_TIME_HEADER = "X-Execution-Time-Ms";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        try {
            MDC.put("requestId", requestId);
            MDC.put("method", request.getMethod());
            MDC.put("path", request.getRequestURI());

            response.setHeader(REQUEST_ID_HEADER, requestId);

            chain.doFilter(request, response);
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            response.setHeader(EXECUTION_TIME_HEADER, String.valueOf(executionTime));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Request {} {} completed in {} ms",
                        request.getMethod(), request.getRequestURI(), executionTime);
            }

            MDC.remove("requestId");
            MDC.remove("method");
            MDC.remove("path");
            MDC.remove("userId");
            MDC.remove("sessionId");
        }
    }
}
