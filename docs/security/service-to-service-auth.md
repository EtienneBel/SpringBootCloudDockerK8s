# Service-to-Service Authentication

## Overview

Backend services authenticate with each other using OAuth2 Client Credentials flow.

## Flow

```
OrderService → Need to call ProductService
             ↓
OAuth2 Interceptor → Request token from Auth0
             ↓
Auth0 → Validate client credentials
             ↓
Return access token
             ↓
Add Authorization header
             ↓
ProductService validates token
```

## Configuration

**OrderService (OAuth2 Client)**:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          internal-client:
            provider: auth0
            authorization-grant-type: client_credentials
            client-id: ${AUTH0_M2M_CLIENT_ID}
            client-secret: ${AUTH0_M2M_CLIENT_SECRET}
```

## RestTemplate Interceptor

```java
@Component
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {
    @Autowired
    private OAuth2AuthorizedClientManager clientManager;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
            .withClientRegistrationId("internal-client")
            .principal("internal")
            .build();

        OAuth2AuthorizedClient client = clientManager.authorize(authorizeRequest);

        if (client != null && client.getAccessToken() != null) {
            String token = client.getAccessToken().getTokenValue();
            request.getHeaders().add("Authorization", "Bearer " + token);
        }

        return execution.execute(request, body);
    }
}
```

## Benefits

✅ Automatic token management
✅ Token caching and refresh
✅ No manual token handling
✅ Secure service-to-service communication

## Auth0 Setup

1. Create Machine-to-Machine Application
2. Authorize for your API
3. Copy client credentials to `.env`

---

**Last Updated**: October 5, 2025
