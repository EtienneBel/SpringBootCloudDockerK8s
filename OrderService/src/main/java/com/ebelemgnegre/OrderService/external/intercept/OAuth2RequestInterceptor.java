package com.ebelemgnegre.OrderService.external.intercept;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

@Log4j2
@Configuration
public class OAuth2RequestInterceptor implements RequestInterceptor {
    @Autowired
    private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

    @Override
    public void apply(RequestTemplate template) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("internal-client")
                .principal("internal")
                .build();
        OAuth2AuthorizedClient authorizedClient = oAuth2AuthorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            String accessTokenValue = authorizedClient.getAccessToken().getTokenValue();
            template.header("Authorization", "Bearer " + accessTokenValue);
            log.debug("Successfully added OAuth2 access token to Feign request");
        } else {
            log.error("Failed to obtain OAuth2 access token for Feign client communication");
            OAuth2Error error = new OAuth2Error(
                    "access_token_unavailable",
                    "Unable to obtain access token from OAuth2 authorization server for Feign client",
                    null
            );
            throw new OAuth2AuthenticationException(error);
        }
    }

}
