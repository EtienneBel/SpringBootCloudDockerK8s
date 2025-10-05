package com.ebelemgnegre.OrderService.external.intercept;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.io.IOException;

@Log4j2
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

    public RestTemplateInterceptor(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        this.oAuth2AuthorizedClientManager = oAuth2AuthorizedClientManager;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("internal-client")
                .principal("internal")
                .build();
        OAuth2AuthorizedClient authorizedClient = oAuth2AuthorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            String accessTokenValue = authorizedClient.getAccessToken().getTokenValue();
            request.getHeaders().add("Authorization", "Bearer " + accessTokenValue);
            log.debug("Successfully added OAuth2 access token to request");
        } else {
            log.error("Failed to obtain OAuth2 access token for service-to-service communication");
            OAuth2Error error = new OAuth2Error(
                    "access_token_unavailable",
                    "Unable to obtain access token from OAuth2 authorization server",
                    null
            );
            throw new OAuth2AuthenticationException(error);
        }

        return execution.execute(request, body);
    }
}
