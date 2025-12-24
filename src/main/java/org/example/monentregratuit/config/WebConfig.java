package org.example.monentregratuit.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {
    // CORS configuration moved to SecurityConfig to avoid conflicts
    // All CORS settings are now handled in SecurityConfig.corsConfigurationSource()
}
