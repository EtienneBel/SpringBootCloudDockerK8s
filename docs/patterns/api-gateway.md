# API Gateway Pattern

## Overview

**Pattern**: Single Entry Point, Gateway Routing
**Tool**: Spring Cloud Gateway (WebFlux-based, non-blocking)
**Port**: 9090
**Location**: `CloudGateway/`

## What is API Gateway?

An API Gateway is a single entry point for all clients. It sits between clients and backend microservices, handling request routing, authentication, load balancing, and other cross-cutting concerns.

## Architecture

```
┌──────────────┐
│   Clients    │
│ (Web/Mobile) │
└──────┬───────┘
       │
       │ Single Entry Point
       ▼
┌─────────────────────────────┐
│    Cloud Gateway (9090)     │
│ ┌─────────────────────────┐ │
│ │  Authentication (JWT)   │ │
│ │  Rate Limiting          │ │
│ │  Circuit Breaker        │ │
│ │  CORS Handling          │ │
│ │  Request Routing        │ │
│ │  Load Balancing         │ │
│ └─────────────────────────┘ │
└──────┬──────────┬───────────┘
       │          │
   ┌───┴──┐   ┌──┴────┐
   ▼      ▼   ▼       ▼
┌────┐ ┌────┐ ┌────┐
│Prod│ │Order│ │Pay│
│Svc │ │Svc  │ │Svc│
└────┘ └────┘ └────┘
```

## Key Features

### 1. Dynamic Routing

Routes requests to appropriate microservices based on URL patterns:

```yaml
spring:
  cloud:
    gateway:
      routes:
        # Product Service Route
        - id: PRODUCT-SERVICE
          uri: lb://PRODUCT-SERVICE  # Load balanced via Eureka
          predicates:
            - Path=/product/**       # Match /product/* requests
          filters:
            - StripPrefix=1          # Remove /product prefix
            - name: CircuitBreaker
              args:
                name: PRODUCT-SERVICE
                fallbackuri: forward:/productServiceFallback

        # Order Service Route
        - id: ORDER-SERVICE
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/order/**
          filters:
            - StripPrefix=1
            - name: CircuitBreaker
              args:
                name: ORDER-SERVICE
                fallbackuri: forward:/orderServiceFallback
```

### 2. Load Balancing

Automatically distributes requests across multiple service instances using Eureka:

```
Client Request → Gateway → Eureka Discovery → [Instance1, Instance2, Instance3]
                                             ↓
                                      Round-robin selection
```

### 3. Authentication & Authorization

Validates JWT tokens at the gateway level:

```java
@Bean
public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/swagger-ui/**", "/actuator/**").permitAll()
            .anyExchange().authenticated()
        )
        .oauth2Login(withDefaults())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
        .build();
}
```

### 4. Circuit Breaker Integration

Provides fallback responses when services are down:

```java
@GetMapping("/productServiceFallback")
public String productServiceFallback() {
    return "ProductService is temporarily unavailable. Please try again later.";
}
```

### 5. CORS Handling

Configures cross-origin resource sharing for frontend applications:

```java
@Bean
public CorsWebFilter corsWebFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",  // React
        "http://localhost:4200",  // Angular
        "http://localhost:5173"   // Vite
    ));
    config.setAllowCredentials(true);
    config.addAllowedHeader("*");
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsWebFilter(source);
}
```

## Why Spring Cloud Gateway?

### Reactive & Non-Blocking

Built on Spring WebFlux (Project Reactor), providing:
- Non-blocking I/O
- Better resource utilization
- Higher throughput
- Handles high concurrency

### Spring Cloud Gateway vs Zuul

| Feature | Spring Cloud Gateway | Zuul 1.x |
|---------|---------------------|----------|
| **Reactive** | ✅ Yes (WebFlux) | ❌ No (Servlet-based) |
| **Non-blocking** | ✅ Yes | ❌ No |
| **Performance** | Higher | Lower |
| **Spring Integration** | Native | Third-party |
| **Maintenance** | Active | In maintenance mode |

## Request Flow

```
1. Client Request
   ↓
2. Gateway receives request on port 9090
   ↓
3. Route Matching (Path predicates)
   ↓
4. Pre-filters (Authentication, Rate Limiting)
   ↓
5. Service Discovery (Query Eureka)
   ↓
6. Load Balancing (Select instance)
   ↓
7. Circuit Breaker (Check service health)
   ↓
8. Forward to Service Instance
   ↓
9. Service Processes Request
   ↓
10. Post-filters (Response modification)
    ↓
11. Return Response to Client
```

## Configuration

### Route Predicates

Match requests based on various criteria:

```yaml
routes:
  - id: path-route
    uri: lb://SERVICE
    predicates:
      - Path=/api/**                    # Path matching
      - Method=GET,POST                 # HTTP method
      - Header=X-Request-Id, \d+        # Header matching
      - Query=foo, ba.                  # Query parameter
      - Cookie=sessionId, abc           # Cookie matching
      - Host=**.example.com             # Host matching
      - Before=2025-01-01T00:00:00Z     # Temporal
```

### Route Filters

Modify requests/responses:

```yaml
filters:
  - StripPrefix=1                       # Remove path prefix
  - AddRequestHeader=X-Gateway, CloudGateway
  - AddResponseHeader=X-Response-Time, {timestamp}
  - RewritePath=/old/(?<segment>.*), /new/${segment}
  - SetStatus=401                       # Override status code
  - Retry=3                             # Retry failed requests
  - RequestRateLimiter                  # Rate limiting
```

### Global Filters

Apply to all routes:

```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - AddRequestHeader=X-Gateway-Source, CloudGateway
        - AddResponseHeader=X-Powered-By, Spring Cloud Gateway
```

## Advanced Features

### Request Rate Limiting

Limit requests per user/IP:

```yaml
- name: RequestRateLimiter
  args:
    redis-rate-limiter.replenishRate: 10    # Tokens per second
    redis-rate-limiter.burstCapacity: 20    # Max burst size
    key-resolver: "#{@userKeyResolver}"     # Key extraction
```

```java
@Bean
public KeyResolver userKeyResolver() {
    return exchange -> Mono.just(
        exchange.getRequest()
            .getHeaders()
            .getFirst("X-User-ID")
    );
}
```

### Request/Response Modification

```java
@Component
public class ModifyRequestBodyGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Modify request body
            ServerHttpRequest modifiedRequest = exchange.getRequest()
                .mutate()
                .header("X-Modified", "true")
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }
}
```

### Custom Predicates

```java
@Component
public class CustomRoutePredicateFactory extends AbstractRoutePredicateFactory<Config> {
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            // Custom matching logic
            String userAgent = exchange.getRequest()
                .getHeaders()
                .getFirst("User-Agent");

            return userAgent != null && userAgent.contains("Mobile");
        };
    }
}
```

## Benefits

✅ **Single Entry Point**
- Simplifies client code
- Centralized routing logic
- Easier to manage API versions

✅ **Cross-Cutting Concerns**
- Authentication at one place
- Logging and monitoring
- Rate limiting
- CORS handling

✅ **Service Abstraction**
- Hides internal service structure
- Can change backend without affecting clients
- Service composition

✅ **Protocol Translation**
- HTTP to gRPC
- REST to GraphQL
- WebSocket support

✅ **Performance**
- Non-blocking I/O
- Connection pooling
- Request caching

## Docker Configuration

```yaml
cloudgateway:
  build:
    context: ./CloudGateway
    dockerfile: Dockerfile.dev
  container_name: cloudgateway-dev
  ports:
    - '9090:9090'
  environment:
    - SPRING_PROFILES_ACTIVE=dev
    - EUREKA_SERVER_ADDRESS=http://serviceregistry:8761/eureka
    - CONFIG_SERVER_URL=configserver
    - AUTH0_CLIENT_ID=${AUTH0_CLIENT_ID}
    - AUTH0_CLIENT_SECRET=${AUTH0_CLIENT_SECRET}
    - AUTH0_ISSUER_URI=${AUTH0_ISSUER_URI}
  depends_on:
    configserver:
      condition: service_healthy
```

## Testing the Gateway

### Health Check

```bash
curl http://localhost:9090/actuator/health
```

### Route Testing

```bash
# Test Product Service route
curl -H "Authorization: Bearer ${TOKEN}" \
  http://localhost:9090/product/1

# Test Order Service route
curl -H "Authorization: Bearer ${TOKEN}" \
  -X POST http://localhost:9090/order \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 2}'
```

### View Configured Routes

```bash
curl http://localhost:9090/actuator/gateway/routes
```

## Troubleshooting

### 404 Not Found

**Cause**: Route not configured or predicates not matching

**Solution**:
```bash
# Check configured routes
curl http://localhost:9090/actuator/gateway/routes | jq

# Check gateway logs
docker-compose -f docker-compose.dev.yml logs cloudgateway
```

### 503 Service Unavailable

**Cause**: Backend service not available in Eureka

**Solution**:
```bash
# Check Eureka dashboard
open http://localhost:8761

# Check service health
docker-compose -f docker-compose.dev.yml ps
```

### Circuit Breaker Always Open

**Cause**: Service consistently failing or slow

**Solution**:
```yaml
# Adjust circuit breaker thresholds
resilience4j:
  circuitbreaker:
    instances:
      PRODUCT-SERVICE:
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        slidingWindowSize: 10
```

## Best Practices

✅ **Use Circuit Breakers**
- Prevent cascading failures
- Provide fallback responses
- Monitor circuit state

✅ **Enable Request Logging**
```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
```

✅ **Implement Rate Limiting**
- Protect backend services
- Prevent abuse
- Fair usage

✅ **Use Global Filters Sparingly**
- Only for truly global concerns
- Consider performance impact
- Keep filters stateless

✅ **Monitor Gateway Metrics**
- Request latency
- Error rates
- Circuit breaker state
- Active connections

## Security Considerations

🔒 **Always Validate Tokens at Gateway**
```yaml
spring:
  security:
    oauth2:
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
```

🔒 **Use HTTPS in Production**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
```

🔒 **Implement Request Size Limits**
```yaml
spring:
  codec:
    max-in-memory-size: 10MB
```

## Related Patterns

- **Service Discovery**: Gateway queries Eureka for service instances
- **Circuit Breaker**: Integrated for resilience
- **Load Balancing**: Client-side load balancing
- **Authentication**: Centralized OAuth2/JWT validation

## References

- [Spring Cloud Gateway Documentation](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/)
- [API Gateway Pattern](https://microservices.io/patterns/apigateway.html)
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)

---

**Last Updated**: October 5, 2025
