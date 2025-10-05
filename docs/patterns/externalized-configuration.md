# Externalized Configuration Pattern

## Overview

**Pattern**: Centralized Configuration Management
**Tool**: Spring Cloud Config Server
**Port**: 9296
**Location**: `ConfigServer/`

## What is Externalized Configuration?

Externalized Configuration is a pattern where application configuration is stored outside the application code, typically in a centralized location. This allows configuration changes without rebuilding or redeploying applications.

## Architecture

```
┌─────────────────────────────┐
│   Git Repository            │
│   (Configuration Files)     │
│   - application.yml         │
│   - product-service-dev.yml │
│   - order-service-prod.yml  │
└──────────┬──────────────────┘
           │ (1) Pulls config on startup
           ▼
┌─────────────────────────────┐
│   Config Server (9296)      │
│   - Serves configuration    │
│   - Caches configs          │
│   - Encrypts secrets        │
└──────────┬──────────────────┘
           │ (2) Requests config
           ▼
┌─────────────────────────────┐
│   Microservices             │
│   - ProductService          │
│   - OrderService            │
│   - PaymentService          │
└─────────────────────────────┘
```

## Benefits

✅ **Centralized Management** - All configs in one place
✅ **Environment-Specific** - Different configs for dev/prod
✅ **Version Control** - Git tracks all changes
✅ **Dynamic Refresh** - Update without restart
✅ **Encryption** - Secure sensitive data

## Implementation

### Config Server Setup

**pom.xml**:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

**Application**:
```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

**application.yml**:
```yaml
server:
  port: 9296

spring:
  application:
    name: CONFIG-SERVER
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo
          clone-on-start: true
          default-label: main
```

### Client Configuration

**application.yml**:
```yaml
spring:
  application:
    name: product-service
  config:
    import: configserver:http://configserver:9296
  cloud:
    config:
      fail-fast: true
      retry:
        max-attempts: 6
```

## Configuration Files Structure

```
config-repo/
├── application.yml              # Common to all services
├── application-dev.yml          # Common dev config
├── application-prod.yml         # Common prod config
├── product-service.yml          # ProductService specific
├── product-service-dev.yml      # ProductService dev
├── product-service-prod.yml     # ProductService prod
└── order-service.yml            # OrderService specific
```

## Best Practices

✅ **Use Git for Configuration**
✅ **Encrypt Sensitive Data**
✅ **Environment-Specific Files**
✅ **Version Control Everything**
✅ **Test Configuration Changes**

## References

- [Spring Cloud Config Documentation](https://spring.io/projects/spring-cloud-config)

---

**Last Updated**: October 5, 2025
