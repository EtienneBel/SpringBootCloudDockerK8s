package com.ebelemgnegre.ProductService.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
//@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(
                        authorizeRequest -> authorizeRequest
                                .requestMatchers(
                                        new AntPathRequestMatcher("/swagger-ui.html"),
                                        new AntPathRequestMatcher("/swagger-ui/**"),
                                        new AntPathRequestMatcher("/v3/api-docs/**"),
                                        new AntPathRequestMatcher("/api-docs/**"),
                                        new AntPathRequestMatcher("/api-docs"),
                                        new AntPathRequestMatcher("/actuator/**"),
                                        new AntPathRequestMatcher("/h2-console/**")
                                )
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .oauth2ResourceServer(
                        OAuth2ResourceServerConfigurer::jwt
                );
        return http.build();
    }
}
