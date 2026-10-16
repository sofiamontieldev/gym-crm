package com.epam.gymcrm.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionLoggingFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_ID = "transactionId";
    public static final String HEADER = "X-Transaction-Id";
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String transactionId = resolveTransactionId(request.getHeader(HEADER));
        long started = System.nanoTime();
        MDC.put(TRANSACTION_ID, transactionId);
        response.setHeader(HEADER, transactionId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            if (response.getStatus() >= 400) {
                log.warn("HTTP {} {} completed with status {} in {} ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            } else {
                log.info("HTTP {} {} completed with status {} in {} ms",
                        request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            }
            MDC.remove(TRANSACTION_ID);
        }
    }

    static String resolveTransactionId(String candidate) {
        return candidate != null && SAFE_ID.matcher(candidate).matches()
                ? candidate
                : UUID.randomUUID().toString();
    }
}
