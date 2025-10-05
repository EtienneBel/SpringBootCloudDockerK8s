# Spring Cloud

## Overview

Spring Cloud provides tools for building cloud-native applications.

## Version

**Spring Cloud**: 2023.0.0

## Components Used

| Component | Purpose |
|-----------|---------|
| **Config Server** | Centralized configuration |
| **Eureka** | Service discovery |
| **Gateway** | API gateway |
| **LoadBalancer** | Client-side load balancing |
| **Circuit Breaker** | Resilience4j integration |
| **OpenFeign** | Declarative REST client |

## Dependencies

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2023.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

**Last Updated**: October 5, 2025
