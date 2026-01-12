package org.example.monentregratuit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip JWT authentication for public endpoints
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        System.out.println("=== JwtAuthenticationFilter ===");
        System.out.println("Request: " + method + " " + requestPath);
        
        if (isPublicEndpoint(requestPath, method)) {
            System.out.println("Public endpoint - skipping JWT authentication");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("Protected endpoint - checking JWT token");
        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization header present: " + (authorizationHeader != null));

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            System.out.println("JWT token extracted (first 20 chars): " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("Username extracted from token: " + username);
            } catch (Exception e) {
                System.err.println("Token extraction failed: " + e.getMessage());
            }
        } else {
            System.out.println("No valid Authorization header found");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("Validating token for user: " + username);
            if (jwtUtil.validateToken(jwt, username)) {
                System.out.println("Token valid - setting authentication");
                // Create authentication with ADMIN authority
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, 
                            java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Authentication set successfully");
            } else {
                System.err.println("Token validation failed for user: " + username);
            }
        } else {
            System.out.println("Skipping authentication - username: " + username + ", existing auth: " + (SecurityContextHolder.getContext().getAuthentication() != null));
        }

        System.out.println("=== Proceeding with filter chain ===");
        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path, String method) {
        // Public endpoints that don't require authentication
        if (path.startsWith("/api/auth/")) return true;
        if (path.startsWith("/api/health")) return true;
        if (path.equals("/api/sliders/active")) return true;
        if (path.equals("/api/sliders/ordered")) return true;
        if (path.startsWith("/api/foires/getAllActive")) return true;
        if (path.equals("/api/settings/about-us/active")) return true;
        if (path.equals("/api/settings/videos/active")) return true;
        if (path.equals("/api/settings/social-links")) return true;
        
        // Public unsubscribe endpoint
        if (path.startsWith("/api/public/unsubscribe")) return true;
        
        // Public newsletter subscribe endpoint
        if (path.equals("/api/newsletter-subscribers/subscribe") && "POST".equalsIgnoreCase(method)) return true;
        
        // Public visit tracking endpoint
        if (path.equals("/api/visits/track") && "POST".equalsIgnoreCase(method)) return true;
        
        // Public template viewer by slug - no auth required
        if (path.startsWith("/api/custom-templates/slug/") && "GET".equalsIgnoreCase(method)) return true;
        
        // POST to /api/reservations is public
        if (path.equals("/api/reservations") && "POST".equalsIgnoreCase(method)) return true;
        
        return false;
    }
}
