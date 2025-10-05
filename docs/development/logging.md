# Logging

## Overview

Structured logging configuration for microservices.

## Log Levels

- **TRACE** - Very detailed
- **DEBUG** - Detailed for debugging
- **INFO** - General information
- **WARN** - Warning messages
- **ERROR** - Error messages

## Configuration

**application.yml**:
```yaml
logging:
  level:
    root: INFO
    com.ebelemgnegre: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: TRACE
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: logs/application.log
```

## Lombok @Log4j2

```java
@Log4j2
@RestController
public class ProductController {

    public Product createProduct(Product product) {
        log.debug("Creating product: {}", product);
        log.info("Product created with ID: {}", product.getId());
        return product;
    }
}
```

## Log Output

```
2025-10-05 12:00:00 - Creating product: Product(id=null, name=Laptop)
2025-10-05 12:00:01 - Product created with ID: 1
```

## Viewing Logs

```bash
# Docker Compose
docker-compose -f docker-compose.dev.yml logs -f productservice

# Filter logs
docker-compose logs productservice | grep -i error

# Kubernetes
kubectl logs -f deployment/productservice

# Tail last 100 lines
kubectl logs --tail=100 deployment/productservice
```

## Log Aggregation (Future)

**ELK Stack** (Elasticsearch, Logstash, Kibana):
```yaml
logging:
  config: classpath:logback-spring.xml
```

**Logback configuration** with JSON output for ELK.

## Best Practices

✅ Use appropriate log levels
✅ Include context (user ID, request ID)
✅ Never log sensitive data (passwords, tokens)
✅ Use structured logging (JSON in production)
✅ Implement correlation IDs for tracing

## Request ID Tracing

Add filter to generate request ID:

```java
@Component
public class RequestIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }
}
```

---

**Last Updated**: October 5, 2025
