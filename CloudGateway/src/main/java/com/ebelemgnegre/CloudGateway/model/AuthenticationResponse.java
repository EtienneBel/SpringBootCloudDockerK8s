package com.ebelemgnegre.CloudGateway.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Authentication response containing user information and OAuth2 tokens")
public class AuthenticationResponse {

    @Schema(
            description = "User identifier (email address from Auth0)",
            example = "user@example.com",
            required = true
    )
    private String userId;

    @Schema(
            description = "OAuth2 access token (JWT) for API authentication. Use this in Authorization header as 'Bearer <token>'",
            example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
    )
    private String accessToken;

    @Schema(
            description = "OAuth2 refresh token for obtaining new access tokens when they expire",
            example = "v1.MRrT...",
            required = false
    )
    private String refreshToken;

    @Schema(
            description = "Token expiration time as Unix epoch timestamp (seconds since 1970-01-01)",
            example = "1759598400",
            required = true
    )
    private long expiresAt;

    @Schema(
            description = "List of user authorities/roles granted by Auth0",
            example = "[\"ROLE_USER\", \"SCOPE_openid\", \"SCOPE_profile\", \"SCOPE_email\"]",
            required = false
    )
    private Collection<String> auhorityList;

}
