# Lombok

## Overview

Lombok reduces boilerplate code through annotations.

## Common Annotations

### @Data

Generates getters, setters, toString, equals, hashCode:

```java
@Data
public class Product {
    private Long id;
    private String name;
    private BigDecimal price;
}
```

### @Builder

Builder pattern:

```java
@Builder
public class Order {
    private Long id;
    private Long productId;
    private Integer quantity;
}

// Usage
Order order = Order.builder()
    .productId(1L)
    .quantity(2)
    .build();
```

### @AllArgsConstructor / @NoArgsConstructor

```java
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    private Long id;
    private BigDecimal amount;
}
```

### @Log4j2

Logger field:

```java
@Log4j2
@RestController
public class ProductController {
    public void createProduct(Product product) {
        log.info("Creating product: {}", product);
    }
}
```

## Configuration

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

---

**Last Updated**: October 5, 2025
