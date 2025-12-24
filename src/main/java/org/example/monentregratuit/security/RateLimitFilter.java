package org.example.monentregratuit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long WINDOW_SIZE_MS = 60000; // 1 minute

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    private static class RequestCounter {
        AtomicInteger count = new AtomicInteger(0);
        long windowStart = System.currentTimeMillis();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only apply rate limiting to login endpoint
        if (!request.getRequestURI().contains("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIP(request);
        RequestCounter counter = requestCounts.computeIfAbsent(clientIp, k -> new RequestCounter());

        long currentTime = System.currentTimeMillis();
        
        // Reset counter if window has passed
        if (currentTime - counter.windowStart > WINDOW_SIZE_MS) {
            counter.count.set(0);
            counter.windowStart = currentTime;
        }

        if (counter.count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Too many login attempts. Please try again later.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
