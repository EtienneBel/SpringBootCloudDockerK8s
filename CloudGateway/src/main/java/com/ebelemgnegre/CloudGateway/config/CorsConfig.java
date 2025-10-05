package com.ebelemgnegre.CloudGateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Allow specific origins (add your frontend URLs)
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // React default
                "http://localhost:4200",  // Angular default
                "http://localhost:8080",  // Vue default
                "http://localhost:5173"   // Vite default
        ));

        // Allow credentials (cookies, authorization headers)
        corsConfig.setAllowCredentials(true);

        // Allow all headers
        corsConfig.addAllowedHeader("*");

        // Allow all HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Max age for preflight cache (1 hour)
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
