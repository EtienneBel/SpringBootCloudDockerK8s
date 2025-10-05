package com.ebelemgnegre.CloudGateway.controller;

import com.ebelemgnegre.CloudGateway.model.AuthenticationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/authenticate")
@Tag(name = "Authentication", description = "OAuth2/OIDC authentication endpoints for user login and token management")
public class AuthenticationController {

    @Operation(
            summary = "OAuth2 Login",
            description = "Initiates OAuth2/OIDC login flow with Auth0. Redirects to Auth0 login page, then returns authenticated user details with access and refresh tokens. " +
                    "Use this for interactive browser-based authentication. For API-to-API calls, use Client Credentials flow instead."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated. Returns user information and OAuth2 tokens.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication failed or user not logged in",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirect to Auth0 login page (if not already authenticated)",
                    content = @Content
            )
    })
    @GetMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Parameter(hidden = true) @AuthenticationPrincipal OidcUser oidcUser,
            @Parameter(hidden = true) @RegisteredOAuth2AuthorizedClient("auth0") OAuth2AuthorizedClient client,
            @Parameter(hidden = true) Model model){
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .userId(oidcUser.getEmail())
                .accessToken(client.getAccessToken().getTokenValue())
                .refreshToken(client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null)
                .expiresAt(client.getAccessToken().getExpiresAt().getEpochSecond())
                .auhorityList(oidcUser.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .build();
        oidcUser.getAuthorities().forEach(authority -> log.info("Authority: {}", authority.getAuthority()));

        return new ResponseEntity<>(authenticationResponse, HttpStatus.OK);
    }
}
