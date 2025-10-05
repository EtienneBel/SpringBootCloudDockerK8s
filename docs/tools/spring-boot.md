# Spring Boot

## Overview

Spring Boot makes it easy to create stand-alone, production-grade Spring applications.

## Version

- **Spring Boot**: 3.2.x
- **Java**: 17

## Key Features

✅ **Auto-Configuration** - Automatic configuration based on dependencies
✅ **Embedded Server** - Tomcat/Netty embedded (no WAR deployment)
✅ **Starter Dependencies** - Curated dependency sets
✅ **Production Ready** - Actuator for metrics and monitoring
✅ **No XML** - Pure Java configuration

## Common Starters

```xml
<!-- Web MVC -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- WebFlux (Reactive) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## Application Structure

```java
@SpringBootApplication
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```

---

**Last Updated**: October 5, 2025
