# Circuit Breaker Pattern

## Overview

**Pattern**: Fault Tolerance, Resilience
**Tool**: Resilience4j (Reactor-based)
**Location**:
- Gateway routes: `CloudGateway/src/main/resources/application-dev.yml`
- Fallback controller: `CloudGateway/controller/FallbackController.java`

## What is Circuit Breaker?

The Circuit Breaker pattern prevents cascading failures in distributed systems by stopping requests to a failing service and providing fallback responses instead. Think of it like an electrical circuit breaker that stops electrical flow when there's a problem.

## Why Circuit Breaker?

Without circuit breaker:
```
Client → Gateway → Failing Service (timeout: 30s)
                   ↓
                 Wait... wait... wait... (each request waits 30s)
                   ↓
                 Eventually: 500 Internal Server Error
```

With circuit breaker:
```
Client → Gateway → Circuit Breaker
                   ↓
                 ✅ Healthy? → Forward to service
                 ❌ Failing? → Return fallback immediately (no wait)
```

## Circuit States

```
┌─────────────┐
│             │  All requests pass through
│   CLOSED    │  (Normal operation)
│             │
└──────┬──────┘
       │ Failure threshold reached
       │ (e.g., 50% failures)
       ▼
┌─────────────┐
│             │  All requests fail fast
│    OPEN     │  (Service protection)
│             │  Return fallback immediately
└──────┬──────┘
       │ Wait duration elapsed
       │ (e.g., 30 seconds)
       ▼
┌─────────────┐
│             │  Limited requests allowed
│  HALF-OPEN  │  (Testing recovery)
│             │
└──────┬──────┘
       │
       ├─ Success → CLOSED (Service recovered)
       └─ Failure → OPEN (Still failing)
```

## Implementation

### 1. Gateway Route Configuration

**Location**: `CloudGateway/src/main/resources/application-dev.yml`

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: PRODUCT-SERVICE
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/product/**
          filters:
            - StripPrefix=1
            - name: CircuitBreaker
              args:
                name: PRODUCT-SERVICE
                fallbackuri: forward:/productServiceFallback

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

        - id: PAYMENT-SERVICE
          uri: lb://PAYMENT-SERVICE
          predicates:
            - Path=/payment/**
          filters:
            - StripPrefix=1
            - name: CircuitBreaker
              args:
                name: PAYMENT-SERVICE
                fallbackuri: forward:/paymentServiceFallback
```

### 2. Fallback Controller

**Location**: `CloudGateway/controller/FallbackController.java`

```java
@RestController
@Tag(name = "Circuit Breaker Fallbacks")
public class FallbackController {

    @Operation(
        summary = "Order Service Fallback",
        description = "Returns when Order Service is down or circuit is open"
    )
    @GetMapping("/orderServiceFallback")
    public String orderServiceFallback() {
        return "OrderService is temporarily unavailable. Please try again later.";
    }

    @GetMapping("/paymentServiceFallback")
    public String paymentServiceFallback() {
        return "PaymentService is temporarily unavailable. Please try again later.";
    }

    @GetMapping("/productServiceFallback")
    public String productServiceFallback() {
        return "ProductService is temporarily unavailable. Please try again later.";
    }
}
```

### 3. Advanced Fallback with Context

```java
@GetMapping("/productServiceFallback")
public ResponseEntity<ProductResponse> productServiceFallback(
        ServerWebExchange exchange) {

    // Access original request details
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();

    // Log the failure
    log.error("Circuit breaker opened for path: {}", path);

    // Return structured fallback response
    ProductResponse fallback = ProductResponse.builder()
        .id(null)
        .name("Service Unavailable")
        .price(BigDecimal.ZERO)
        .available(false)
        .message("Product service is temporarily down. Please try again later.")
        .build();

    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(fallback);
}
```

## Resilience4j Configuration

### Basic Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      PRODUCT-SERVICE:
        # Failure rate threshold (percentage)
        failureRateThreshold: 50

        # Minimum number of calls before calculating failure rate
        minimumNumberOfCalls: 5

        # Wait duration in open state before attempting half-open
        waitDurationInOpenState: 30s

        # Number of permitted calls in half-open state
        permittedNumberOfCallsInHalfOpenState: 3

        # Sliding window size for failure rate calculation
        slidingWindowSize: 10

        # Sliding window type (COUNT_BASED or TIME_BASED)
        slidingWindowType: COUNT_BASED

        # Slow call duration threshold
        slowCallDurationThreshold: 5s

        # Slow call rate threshold
        slowCallRateThreshold: 50

        # Record exceptions that should count as failures
        recordExceptions:
          - org.springframework.web.client.HttpServerErrorException
          - java.util.concurrent.TimeoutException
          - java.io.IOException

        # Ignore exceptions (don't count as failures)
        ignoreExceptions:
          - org.springframework.web.client.HttpClientErrorException
```

### Configuration Explained

| Setting | Description | Example |
|---------|-------------|---------|
| **failureRateThreshold** | Percentage of failures to open circuit | 50% |
| **minimumNumberOfCalls** | Min calls before evaluating threshold | 5 calls |
| **waitDurationInOpenState** | How long to stay open before testing | 30 seconds |
| **permittedNumberOfCallsInHalfOpenState** | Test calls in half-open | 3 calls |
| **slidingWindowSize** | Window for failure calculation | Last 10 calls |
| **slowCallDurationThreshold** | Max duration before considering slow | 5 seconds |

## Flow Diagrams

### Normal Operation (Circuit CLOSED)

```
Client Request
    ↓
Gateway
    ↓
Circuit Breaker (CLOSED)
    ↓
ProductService ✅ (Success - 200ms)
    ↓
Response to Client
```

### Service Failure (Circuit OPEN)

```
Client Request
    ↓
Gateway
    ↓
Circuit Breaker (OPEN) ⚠️
    ↓
Skip service call
    ↓
Fallback Controller
    ↓
"ProductService is down" (Immediate response)
```

### Recovery Testing (Circuit HALF-OPEN)

```
Client Request
    ↓
Gateway
    ↓
Circuit Breaker (HALF-OPEN) 🟡
    ↓
ProductService (Test call)
    ↓
✅ Success? → CLOSED (Recovered)
❌ Failure? → OPEN (Still down)
```

## Benefits

✅ **Prevent Cascading Failures**
```
Without CB: Service A down → Service B times out → Service C times out → Entire system down
With CB:    Service A down → Circuit opens → Immediate fallback → Other services OK
```

✅ **Fast Failure**
- No waiting for timeouts
- Immediate fallback response
- Better user experience

✅ **Service Protection**
- Gives failing service time to recover
- Reduces load on unhealthy service
- Prevents resource exhaustion

✅ **Graceful Degradation**
- System continues to function
- Reduced functionality instead of complete failure
- Better than nothing

## Monitoring Circuit Breaker

### Actuator Endpoints

Enable circuit breaker metrics:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,circuitbreakers,circuitbreakerevents
  health:
    circuitbreakers:
      enabled: true
```

### Check Circuit State

```bash
# View all circuit breakers
curl http://localhost:9090/actuator/circuitbreakers

# Response:
{
  "circuitBreakers": {
    "PRODUCT-SERVICE": {
      "state": "CLOSED",
      "failureRate": 10.5,
      "slowCallRate": 5.2,
      "bufferedCalls": 10,
      "failedCalls": 1,
      "slowCalls": 0,
      "notPermittedCalls": 0
    }
  }
}
```

### Circuit Breaker Events

```bash
# View recent events
curl http://localhost:9090/actuator/circuitbreakerevents

# Response:
{
  "circuitBreakerEvents": [
    {
      "circuitBreakerName": "PRODUCT-SERVICE",
      "type": "ERROR",
      "creationTime": "2025-10-05T12:00:00Z",
      "errorMessage": "Connection timeout",
      "duration": 5000
    },
    {
      "circuitBreakerName": "PRODUCT-SERVICE",
      "type": "STATE_TRANSITION",
      "creationTime": "2025-10-05T12:00:30Z",
      "stateTransition": "CLOSED_TO_OPEN"
    }
  ]
}
```

## Testing Circuit Breaker

### Simulate Service Failure

```bash
# Stop ProductService
docker-compose -f docker-compose.dev.yml stop productservice

# Make requests (should get fallback)
curl http://localhost:9090/product/1

# Response: "ProductService is temporarily unavailable"
```

### Monitor State Transitions

```bash
# Watch circuit breaker events
watch -n 1 'curl -s http://localhost:9090/actuator/circuitbreakers | jq'
```

### Test Recovery

```bash
# Restart ProductService
docker-compose -f docker-compose.dev.yml start productservice

# Wait for circuit to go HALF-OPEN (30 seconds)
# Make successful requests
# Circuit should transition to CLOSED
```

## Advanced Patterns

### Fallback with Cached Data

```java
@Component
public class ProductFallbackService {

    @Autowired
    private RedisTemplate<String, Product> redisTemplate;

    public Product getProductFallback(Long productId) {
        // Try to return cached data
        Product cached = redisTemplate.opsForValue()
            .get("product:" + productId);

        if (cached != null) {
            log.info("Returning cached product: {}", productId);
            return cached;
        }

        // Return default product
        return Product.builder()
            .id(productId)
            .name("Product Unavailable")
            .price(BigDecimal.ZERO)
            .build();
    }
}
```

### Retry Before Opening Circuit

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: PRODUCT-SERVICE
          filters:
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,GATEWAY_TIMEOUT
                backoff:
                  firstBackoff: 100ms
                  maxBackoff: 1s
                  factor: 2
            - name: CircuitBreaker
              args:
                name: PRODUCT-SERVICE
                fallbackuri: forward:/productServiceFallback
```

### Bulkhead Pattern (Resource Isolation)

Limit concurrent calls to prevent resource exhaustion:

```yaml
resilience4j:
  bulkhead:
    instances:
      PRODUCT-SERVICE:
        maxConcurrentCalls: 10
        maxWaitDuration: 100ms
```

```java
@Bulkhead(name = "PRODUCT-SERVICE", fallbackMethod = "fallbackProduct")
public Product getProduct(Long id) {
    return restTemplate.getForObject(
        "lb://PRODUCT-SERVICE/product/" + id,
        Product.class
    );
}
```

## Best Practices

✅ **Set Appropriate Thresholds**
```yaml
# Too sensitive: Opens too easily
failureRateThreshold: 10  # ❌

# Too lenient: Doesn't protect
failureRateThreshold: 90  # ❌

# Just right: Balanced
failureRateThreshold: 50  # ✅
```

✅ **Provide Meaningful Fallbacks**
```java
// ❌ Bad: Generic error
return "Error";

// ✅ Good: Helpful message
return "OrderService is temporarily unavailable. " +
       "Your cart is saved. Please try again in a few minutes.";
```

✅ **Log Circuit Events**
```java
@EventListener
public void onCircuitBreakerEvent(CircuitBreakerEvent event) {
    if (event.getEventType() == CircuitBreakerEvent.Type.STATE_TRANSITION) {
        log.warn("Circuit breaker {} transitioned from {} to {}",
            event.getCircuitBreakerName(),
            event.getStateTransition().getFromState(),
            event.getStateTransition().getToState()
        );
    }
}
```

✅ **Monitor and Alert**
- Set up alerts when circuits open
- Track failure rates
- Monitor recovery times

## Troubleshooting

### Circuit Never Opens

**Problem**: Service failing but circuit stays closed

**Causes**:
- `minimumNumberOfCalls` not reached
- Failure rate below threshold
- Exceptions not recorded

**Solution**:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      SERVICE:
        minimumNumberOfCalls: 5  # Lower threshold
        failureRateThreshold: 30  # More sensitive
        recordExceptions:
          - java.lang.Exception  # Record all exceptions
```

### Circuit Always Open

**Problem**: Circuit opens immediately and won't close

**Causes**:
- Service actually down
- Threshold too low
- `waitDurationInOpenState` too long

**Solution**:
```bash
# Check service health
docker-compose ps

# Check circuit breaker state
curl http://localhost:9090/actuator/circuitbreakers

# Adjust configuration
waitDurationInOpenState: 10s  # Shorter wait time
```

### Fallback Not Triggered

**Problem**: Getting 500 errors instead of fallback

**Causes**:
- Fallback URI incorrect
- Fallback controller not found
- Exception not caught

**Solution**:
```yaml
# Ensure correct fallback URI
fallbackuri: forward:/productServiceFallback  # Must match @GetMapping

# Check controller is registered
curl http://localhost:9090/actuator/mappings | grep -i fallback
```

## Related Patterns

- **Retry Pattern**: Retry before opening circuit
- **Timeout Pattern**: Prevent slow calls
- **Bulkhead Pattern**: Isolate resources
- **Fallback Pattern**: Alternative responses

## Alternatives

| Tool | Pros | Cons |
|------|------|------|
| **Hystrix** | Mature, feature-rich | Deprecated, in maintenance mode |
| **Resilience4j** | Modern, lightweight, reactive | ✅ Our choice |
| **Sentinel** | Alibaba, powerful | Less Spring integration |
| **Istio** | Service mesh, automatic | Requires Kubernetes |

## References

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Circuit Breaker Pattern - Martin Fowler](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Microservices Patterns](https://microservices.io/patterns/reliability/circuit-breaker.html)

---

**Last Updated**: October 5, 2025
