package com.example.expense_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Allow React Frontend
        configuration.setAllowedOrigins(Arrays.asList(
                "https://expense-tracker-frontend-vree.onrender.com"
        ));

        // Allow HTTP Methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        // Allow Headers
        configuration.setAllowedHeaders(Arrays.asList(
                "*"
        ));

        // Expose Authorization Header
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization"
        ));

        // Allow Cookies/JWT
        configuration.setAllowCredentials(true);

        // Cache Preflight Request
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}