# OpenAPI / Swagger

## Overview

Springdoc OpenAPI generates API documentation from annotations.

## Version

**Springdoc OpenAPI**: 2.2.0

## Dependencies

```xml
<!-- For WebMVC (ProductService, OrderService, PaymentService) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>

<!-- For WebFlux (CloudGateway) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

## Access Swagger UI

- **Centralized**: http://localhost:9090/swagger-ui.html
- **ProductService**: http://localhost:8081/swagger-ui.html
- **OrderService**: http://localhost:8082/swagger-ui.html
- **PaymentService**: http://localhost:8083/swagger-ui.html

## Configuration

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

## Annotations

```java
@Tag(name = "Products", description = "Product management APIs")
@Operation(summary = "Get product by ID")
@ApiResponse(responseCode = "200", description = "Found product")
public Product getProduct(@PathVariable Long id) { }
```

---

**Last Updated**: October 5, 2025
