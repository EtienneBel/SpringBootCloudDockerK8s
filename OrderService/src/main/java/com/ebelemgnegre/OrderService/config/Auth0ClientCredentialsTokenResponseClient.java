package com.ebelemgnegre.OrderService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Base64;
import java.util.Map;

@Component
public class Auth0ClientCredentialsTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> {

    @Value("${AUTH0_AUDIENCE}")
    private String audience;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2ClientCredentialsGrantRequest grantRequest) {
        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add("grant_type", "client_credentials");
        formParameters.add("audience", audience);

        String clientId = grantRequest.getClientRegistration().getClientId();
        String clientSecret = grantRequest.getClientRegistration().getClientSecret();
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedCredentials);

        URI tokenUri = URI.create(grantRequest.getClientRegistration().getProviderDetails().getTokenUri());

        RequestEntity<MultiValueMap<String, String>> requestEntity =
                RequestEntity.post(tokenUri).headers(headers).body(formParameters);

        Map<String, Object> responseMap = restTemplate.exchange(requestEntity, Map.class).getBody();

        return OAuth2AccessTokenResponse
                .withToken((String) responseMap.get("access_token"))
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn((Integer) responseMap.get("expires_in"))
                .build();
    }
}
