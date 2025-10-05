# Service-to-Service Communication

## Overview

**Pattern**: Synchronous REST Communication with OAuth2
**Tools**: RestTemplate + OpenFeign
**Location**: OrderService → ProductService, PaymentService

This project uses **both RestTemplate and OpenFeign** for inter-service communication, with automatic OAuth2 token injection for security.

## Why Two Approaches?

The **OrderService** uses both approaches strategically:

### OpenFeign (Declarative)
**Used for**: Write operations (POST, PUT, DELETE)

✅ **Cleaner syntax** - No boilerplate code
✅ **Type-safe** - Compile-time checking
✅ **Self-documenting** - Interface mirrors API
✅ **Automatic serialization** - JSON handling built-in

### RestTemplate (Imperative)
**Used for**: Read operations (GET) and complex scenarios

✅ **More control** - Explicit configuration
✅ **Error handling** - Fine-grained exception management
✅ **Flexibility** - Custom headers, interceptors
✅ **Debugging** - Easier to trace and log

## Real Implementation Examples

### 1. OpenFeign Clients (Write Operations)

**ProductService Client** (`OrderService/external/client/ProductService.java`):
```java
@FeignClient(name = "PRODUCT-SERVICE/product")
public interface ProductService {
    @PutMapping("/reduceQuantity/{id}")
    ResponseEntity<Void> reduceQuantity(
        @PathVariable("id") long productId,
        @RequestParam long quantity
    );
}
```

**PaymentService Client** (`OrderService/external/client/PaymentService.java`):
```java
@FeignClient(name = "PAYMENT-SERVICE/payment")
public interface PaymentService {
    @PostMapping
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);
}
```

**Usage in OrderServiceImpl**:
```java
@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductService productService;  // Feign client

    @Autowired
    private PaymentService paymentService;  // Feign client

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        // Using Feign for write operations
        productService.reduceQuantity(
            orderRequest.getProductId(),
            orderRequest.getQuantity()
        );

        PaymentRequest paymentRequest = PaymentRequest.builder()
            .orderId(order.getId())
            .paymentMode(orderRequest.getPaymentMode())
            .amount(orderRequest.getTotalAmount())
            .build();

        paymentService.doPayment(paymentRequest);

        return order.getId();
    }
}
```

### 2. RestTemplate (Read Operations)

**Configuration** (`OrderServiceApplication.java`):
```java
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {

    @Bean
    @LoadBalanced  // Enables Eureka service discovery
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(
            Arrays.asList(
                new RestTemplateInterceptor(
                    clientManager(clientRegistrationRepository, oAuth2AuthorizedClientRepository)
                )
            )
        );
        return restTemplate;
    }

    @Bean
    public OAuth2AuthorizedClientManager clientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository
    ) {
        OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder
            .builder()
            .clientCredentials()  // Service-to-service flow
            .refreshToken()       // Auto token refresh
            .build();

        DefaultOAuth2AuthorizedClientManager manager =
            new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                oAuth2AuthorizedClientRepository
            );
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }
}
```

**Usage in OrderServiceImpl**:
```java
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RestTemplate restTemplate;  // Pre-configured with OAuth2

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        // Using RestTemplate for read operations
        ProductResponse productResponse = restTemplate.getForObject(
            "http://PRODUCT-SERVICE/product/" + order.getProductId(),
            ProductResponse.class
        );

        PaymentResponse paymentResponse = restTemplate.getForObject(
            "http://PAYMENT-SERVICE/payment/order/" + order.getId(),
            PaymentResponse.class
        );

        // Build composite response
        return OrderResponse.builder()
            .orderId(order.getId())
            .productDetails(productDetails)
            .paymentDetails(paymentDetails)
            .build();
    }
}
```

## OAuth2 Token Injection

### RestTemplate Interceptor

**RestTemplateInterceptor.java**:
```java
@Log4j2
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

    public RestTemplateInterceptor(OAuth2AuthorizedClientManager manager) {
        this.oAuth2AuthorizedClientManager = manager;
    }

    @Override
    public ClientHttpResponse intercept(
        HttpRequest request,
        byte[] body,
        ClientHttpRequestExecution execution
    ) throws IOException {

        // Request OAuth2 token using Client Credentials flow
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
            .withClientRegistrationId("internal-client")
            .principal("internal")
            .build();

        OAuth2AuthorizedClient authorizedClient =
            oAuth2AuthorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            request.getHeaders().add("Authorization", "Bearer " + accessToken);
            log.debug("Successfully added OAuth2 access token to request");
        } else {
            log.error("Failed to obtain OAuth2 access token");
            throw new OAuth2AuthenticationException(
                new OAuth2Error("access_token_unavailable",
                    "Unable to obtain access token", null)
            );
        }

        return execution.execute(request, body);
    }
}
```

### OpenFeign OAuth2 Configuration

OpenFeign uses the same OAuth2 configuration automatically through Spring Security's integration.

**application.yml**:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          internal-client:
            provider: auth0
            client-id: ${AUTH0_M2M_CLIENT_ID}
            client-secret: ${AUTH0_M2M_CLIENT_SECRET}
            authorization-grant-type: client_credentials
            scope: read:products,write:products,read:payments,write:payments
        provider:
          auth0:
            issuer-uri: https://${AUTH0_DOMAIN}/
```

## How It Works

```
OrderService
    │
    ├─ Place Order (Write)
    │   ├─ OpenFeign → ProductService.reduceQuantity()
    │   │   └─ Auto OAuth2 token injection
    │   │   └─ PUT /product/reduceQuantity/123
    │   │
    │   └─ OpenFeign → PaymentService.doPayment()
    │       └─ Auto OAuth2 token injection
    │       └─ POST /payment
    │
    └─ Get Order Details (Read)
        ├─ RestTemplate → GET /product/123
        │   └─ RestTemplateInterceptor adds Bearer token
        │
        └─ RestTemplate → GET /payment/order/456
            └─ RestTemplateInterceptor adds Bearer token
```

## Service Discovery Integration

Both approaches use **@LoadBalanced** with Eureka:

```java
@Bean
@LoadBalanced  // Enables service name resolution
public RestTemplate restTemplate() { ... }
```

**How it works**:
1. Use service name instead of IP: `http://PRODUCT-SERVICE/product/1`
2. Eureka resolves `PRODUCT-SERVICE` to actual instance IPs
3. Client-side load balancing distributes requests
4. Automatic failover if instance is down

## When to Use Each Approach

| Scenario | Use | Reason |
|----------|-----|--------|
| Simple CRUD operations | **OpenFeign** | Less boilerplate, cleaner code |
| Write operations (POST/PUT/DELETE) | **OpenFeign** | Type-safe request bodies |
| Complex request handling | **RestTemplate** | More control over requests |
| Custom error handling | **RestTemplate** | Fine-grained exception handling |
| Need to inspect responses | **RestTemplate** | Access to ResponseEntity details |
| Aggregating multiple calls | **RestTemplate** | Easier to orchestrate |

## Benefits

✅ **Automatic OAuth2 token management** - No manual token handling
✅ **Service discovery** - Use service names, not IPs
✅ **Load balancing** - Automatic distribution across instances
✅ **Type safety** - Compile-time checking with Feign
✅ **Flexibility** - Choose the right tool for each scenario
✅ **Token refresh** - Automatic renewal before expiration

## Troubleshooting

### Feign Client Not Found
```bash
# Error: No Feign Client for loadBalancing defined
```
**Solution**: Add `@EnableFeignClients` to main application class

### OAuth2 Token Errors
```bash
# Error: access_token_unavailable
```
**Solution**: Verify Auth0 M2M credentials in application.yml

### Service Name Not Resolving
```bash
# Error: Unknown host: PRODUCT-SERVICE
```
**Solution**: Ensure service is registered with Eureka

## References

- [OpenFeign Documentation](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/)
- [RestTemplate OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/authorized-clients.html)
- [OAuth2 Client Credentials](../security/service-to-service-auth.md)
- [Service Discovery](service-discovery.md)

---

**Last Updated**: October 5, 2025
