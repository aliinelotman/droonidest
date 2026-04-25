package com.app.backend.security;

import com.app.backend.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Ensures each request has a correlation ID available in logs and response headers.
 */
@Slf4j
@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        long startNanos = System.nanoTime();
        String correlationId = resolveCorrelationId(request);
        MDC.put(CORRELATION_ID_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            logRequestCompletion(request, response, startNanos);
            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health")
                || path.startsWith("/actuator/prometheus")
                || path.equals("/error");
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String headerValue = request.getHeader(CORRELATION_ID_HEADER);
        if (headerValue == null) {
            return UUID.randomUUID().toString();
        }

        String correlationId = headerValue.trim();
        if (correlationId.isEmpty() || correlationId.length() > 128) {
            return UUID.randomUUID().toString();
        }

        return correlationId;
    }

    private void logRequestCompletion(HttpServletRequest request, HttpServletResponse response, long startNanos) {
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = resolveUserId(authentication);

        log.info("Request completed method={} path={} status={} durationMs={} userId={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs,
                userId);
    }

    private String resolveUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            return "anonymous";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user.getId().toString();
        }

        return authentication.getName();
    }
}
