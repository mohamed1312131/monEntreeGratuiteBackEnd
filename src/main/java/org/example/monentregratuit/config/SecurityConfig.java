package org.example.monentregratuit.config;

import org.example.monentregratuit.security.JwtAuthenticationFilter;
import org.example.monentregratuit.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(frontendUrl, "http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow OPTIONS requests for CORS preflight
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                
                // Authentication endpoints - public
                .requestMatchers("/api/auth/**").permitAll()
                
                // Health check endpoint - public
                .requestMatchers("/api/health").permitAll()
                
                // Frontoffice public endpoints - no authentication required
                .requestMatchers("/api/sliders/active").permitAll()
                .requestMatchers("/api/sliders/ordered").permitAll()
                .requestMatchers("/api/foires/{countryCode}").permitAll()
                .requestMatchers("/api/foires/getAllActive/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/reservations").permitAll()  // POST for creating reservations
                .requestMatchers("/api/settings/about-us/active").permitAll()
                .requestMatchers("/api/settings/videos/active").permitAll()
                .requestMatchers("/api/settings/social-links").permitAll()
                
                // Public unsubscribe endpoint - no authentication required
                .requestMatchers("/api/public/unsubscribe/**").permitAll()
                
                // Public newsletter subscribe endpoint - no authentication required
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/newsletter-subscribers/subscribe").permitAll()
                
                // Public visit tracking endpoint - no authentication required
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/visits/track").permitAll()
                
                // Email tracking endpoints - no authentication required (for pixel/click tracking)
                .requestMatchers("/api/track/**").permitAll()
                
                // Custom template public viewer - no authentication required
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/custom-templates/slug/**").permitAll()
                
                // Custom template admin endpoints - require authentication
                .requestMatchers("/api/custom-templates/**").authenticated()
                
                // Visit statistics endpoints - require authentication (admin only)
                .requestMatchers("/api/visits/**").authenticated()
                
                // Newsletter subscriber endpoints - require authentication (admin only)
                .requestMatchers("/api/newsletter-subscribers/**").authenticated()
                
                // All other endpoints require authentication (admin only)
                .anyRequest().authenticated()
            )
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
