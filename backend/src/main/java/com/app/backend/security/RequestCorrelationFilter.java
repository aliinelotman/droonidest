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
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Ensures each request has a correlation ID available in logs and response headers.
 */
@Slf4j
@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final Pattern CORRELATION_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9._:-]{0,127}$");
    private static final int RANDOM_SUFFIX_LENGTH = 10;

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
            return corralationId();
        }

        String correlationId = headerValue.trim();
        if (!CORRELATION_ID_PATTERN.matcher(correlationId).matches()) {
            return corralationId();
        }

        return correlationId;
    }

    private String corralationId() {
        String timestampPart = Long.toString(Instant.now().toEpochMilli(), 36).toUpperCase(Locale.ROOT);
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, RANDOM_SUFFIX_LENGTH)
                .toUpperCase(Locale.ROOT);
        return "c-" + timestampPart + "-" + randomPart;
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
