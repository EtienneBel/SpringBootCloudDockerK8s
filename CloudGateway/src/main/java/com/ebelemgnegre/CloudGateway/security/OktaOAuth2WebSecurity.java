package com.ebelemgnegre.CloudGateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class OktaOAuth2WebSecurity {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/v3/api-docs/**",
                                "/api-docs",
                                "/api-docs/**",
                                "/*/api-docs/**",
                                "/*/api-docs",
                                "/product/api-docs",
                                "/order/api-docs",
                                "/payment/api-docs",
                                "/actuator/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .csrf(csrf -> csrf.disable())  // Disable CSRF for development (API Gateway typically doesn't need CSRF protection)
                .oauth2Login(withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(withDefaults())
                )
                .build();
    }
}
