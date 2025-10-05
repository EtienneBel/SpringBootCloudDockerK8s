# Service-to-Service Communication

## Overview

**Pattern**: Synchronous REST Communication with OAuth2
**Tools**: RestTemplate, OpenFeign

## RestTemplate with OAuth2

```java
@Bean
public RestTemplate restTemplate(OAuth2AuthorizedClientManager clientManager) {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(
        new RestTemplateInterceptor(clientManager)
    );
    return restTemplate;
}
```

## OpenFeign Client

```java
@FeignClient(name = "PRODUCT-SERVICE", path = "/product")
public interface ProductService {
    @GetMapping("/{id}")
    ProductResponse getProductById(@PathVariable Long id);
}
```

## OAuth2 Interceptor

Automatically adds Authorization header:

```java
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) {
        OAuth2AuthorizedClient client = clientManager.authorize(...);
        String token = client.getAccessToken().getTokenValue();
        request.getHeaders().add("Authorization", "Bearer " + token);
        return execution.execute(request, body);
    }
}
```

## Benefits

✅ Automatic OAuth2 token management
✅ Declarative API clients (Feign)
✅ Load balancing via Eureka
✅ No manual token handling

---

**Last Updated**: October 5, 2025
