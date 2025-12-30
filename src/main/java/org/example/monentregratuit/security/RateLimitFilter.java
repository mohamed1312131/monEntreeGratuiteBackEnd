package org.example.monentregratuit.security;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.monentregratuit.service.RateLimitService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = rateLimitService.getClientIP(request);

        Bucket bucket = null;
        String endpoint = "";

        // Apply rate limiting to specific public endpoints
        if (uri.contains("/api/reservations") && method.equals("POST")) {
            bucket = rateLimitService.resolveReservationBucket(clientIp);
            endpoint = "reservation";
        } else if (uri.contains("/api/newsletter-subscribers/subscribe") && method.equals("POST")) {
            bucket = rateLimitService.resolveNewsletterBucket(clientIp);
            endpoint = "newsletter subscription";
        } else if (uri.contains("/api/exposant-requests") && method.equals("POST")) {
            bucket = rateLimitService.resolveExposantBucket(clientIp);
            endpoint = "exposant request";
        } else if (uri.contains("/api/newsletter-subscribers/send-bulk-email") && method.equals("POST")) {
            bucket = rateLimitService.resolveBulkEmailBucket(clientIp);
            endpoint = "bulk email";
        }

        // If rate limiting applies and limit exceeded
        if (bucket != null && !rateLimitService.allowRequest(bucket)) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many " + endpoint + " requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
