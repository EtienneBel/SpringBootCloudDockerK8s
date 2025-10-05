# Service Discovery Pattern

## Overview

**Pattern**: Client-Side Service Discovery
**Tool**: Netflix Eureka Server
**Port**: 8761
**Location**: `ServiceRegistry/`

## What is Service Discovery?

Service Discovery is a pattern that allows microservices to find and communicate with each other without hardcoded IP addresses or ports. In a dynamic cloud environment where services scale up/down and IPs change frequently, service discovery becomes essential.

## How It Works

```
┌─────────────────────┐
│  Service Instance   │
│  (ProductService)   │
└──────────┬──────────┘
           │ (1) Register on startup
           ▼
┌─────────────────────┐
│  Eureka Server      │
│  (ServiceRegistry)  │
│   Port 8761         │
└──────────┬──────────┘
           │ (2) Heartbeat every 30s
           │
           │ (3) Query for service instances
           ▼
┌─────────────────────┐
│  Client Service     │
│  (OrderService)     │
└─────────────────────┘
```

### Registration Process

1. **Service Startup**: When a service starts, it registers with Eureka Server
2. **Heartbeat**: Service sends periodic heartbeats (default: 30 seconds)
3. **Health Monitoring**: Eureka monitors service health
4. **Deregistration**: Automatic when service stops or fails health checks

### Discovery Process

1. **Client Request**: Service needs to call another service
2. **Query Eureka**: Client queries Eureka for available instances
3. **Load Balance**: Client selects an instance (round-robin by default)
4. **Direct Call**: Client makes direct HTTP call to selected instance

## Implementation

### Eureka Server Configuration

**Location**: `ServiceRegistry/src/main/resources/application.yml`

```yaml
server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false  # Server doesn't register with itself
    fetchRegistry: false       # Server doesn't fetch registry
    serviceUrl:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

**Main Application**:

```java
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
```

### Eureka Client Configuration

**All Services** (ProductService, OrderService, PaymentService, CloudGateway):

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://serviceregistry:8761/eureka
    registerWithEureka: true
    fetchRegistry: true
  instance:
    preferIpAddress: true
    instanceId: ${spring.application.name}:${random.value}
```

**Main Application**:

```java
@SpringBootApplication
@EnableEurekaClient
public class ProductServiceApplication {
    public static final void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```

## Benefits

✅ **Dynamic Service Discovery**
- No hardcoded service URLs
- Services can be added/removed dynamically
- Automatic failover to healthy instances

✅ **Load Distribution**
- Client-side load balancing
- Distributes requests across multiple instances
- Better resource utilization

✅ **Health Monitoring**
- Automatic health checks
- Unhealthy instances removed from registry
- Self-healing architecture

✅ **Zero Downtime Deployments**
- New instances register while old ones deregister
- Gradual traffic shift
- No service interruption

## Usage in Gateway

**CloudGateway Routes**:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: PRODUCT-SERVICE
          uri: lb://PRODUCT-SERVICE  # Load balanced via Eureka
          predicates:
            - Path=/product/**
```

The `lb://` prefix tells Spring Cloud Gateway to:
1. Query Eureka for `PRODUCT-SERVICE` instances
2. Load balance requests across available instances
3. Automatically route to healthy instances

## Eureka Dashboard

Access the Eureka Dashboard at: **http://localhost:8761**

**Dashboard Shows**:
- All registered services
- Number of instances per service
- Instance health status
- Uptime
- Last heartbeat time

## Configuration Options

### Instance Registration

```yaml
eureka:
  instance:
    # Use IP address instead of hostname
    preferIpAddress: true

    # Custom instance ID
    instanceId: ${spring.application.name}:${random.value}

    # Lease renewal interval (heartbeat)
    leaseRenewalIntervalInSeconds: 30

    # Lease expiration duration
    leaseExpirationDurationInSeconds: 90

    # Custom metadata
    metadata-map:
      version: 1.0.0
      environment: dev
```

### Client Configuration

```yaml
eureka:
  client:
    # Enable/disable registration
    registerWithEureka: true

    # Enable/disable fetching registry
    fetchRegistry: true

    # Registry fetch interval
    registryFetchIntervalSeconds: 30

    # Connection timeout
    eurekaServerConnectTimeoutSeconds: 5

    # Read timeout
    eurekaServerReadTimeoutSeconds: 8
```

## Health Checks

### Default Health Check

Eureka uses heartbeat mechanism:
- Service sends heartbeat every 30 seconds
- If 3 consecutive heartbeats fail (90 seconds), instance is removed

### Custom Health Check

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Custom health logic
        boolean healthy = checkDatabaseConnection() && checkExternalAPI();

        if (healthy) {
            return Health.up()
                .withDetail("database", "connected")
                .withDetail("externalAPI", "reachable")
                .build();
        } else {
            return Health.down()
                .withDetail("error", "Service unhealthy")
                .build();
        }
    }
}
```

## Docker Compose Configuration

```yaml
serviceregistry:
  build:
    context: ./ServiceRegistry
    dockerfile: Dockerfile.dev
  container_name: serviceregistry-dev
  ports:
    - '8761:8761'
  environment:
    - SPRING_PROFILES_ACTIVE=dev
  networks:
    - dev-network
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
    interval: 10s
    timeout: 5s
    retries: 10
```

## Troubleshooting

### Service Not Showing in Eureka

**Possible Causes**:
1. Service not started
2. Eureka client not configured properly
3. Network connectivity issues
4. Firewall blocking communication

**Solutions**:
```bash
# Check service logs
docker-compose -f docker-compose.dev.yml logs productservice

# Check Eureka connectivity
curl http://localhost:8761/eureka/apps

# Verify environment variables
docker-compose -f docker-compose.dev.yml exec productservice env | grep EUREKA
```

### Instance Marked as DOWN

**Possible Causes**:
1. Service health check failing
2. Network issues
3. Service overloaded

**Solutions**:
```bash
# Check service health
curl http://localhost:8081/actuator/health

# Check service logs
docker-compose -f docker-compose.dev.yml logs productservice | grep -i health

# Restart service
docker-compose -f docker-compose.dev.yml restart productservice
```

### Multiple Instances Not Load Balancing

**Possible Causes**:
1. Instances have same instance ID
2. Load balancer not configured
3. Ribbon/LoadBalancer disabled

**Solutions**:
```yaml
# Ensure unique instance IDs
eureka:
  instance:
    instanceId: ${spring.application.name}:${random.value}
```

## Best Practices

✅ **Use Unique Instance IDs**
```yaml
instanceId: ${spring.application.name}:${random.value}
```

✅ **Prefer IP Addresses in Docker**
```yaml
preferIpAddress: true
```

✅ **Set Appropriate Timeouts**
```yaml
leaseRenewalIntervalInSeconds: 30
leaseExpirationDurationInSeconds: 90
```

✅ **Use Health Checks**
```yaml
# Expose health endpoint
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

✅ **Monitor Eureka Dashboard**
- Regularly check service registration
- Monitor instance count
- Watch for DOWN instances

## Alternatives

| Tool | Pros | Cons |
|------|------|------|
| **Consul** | Service mesh features, key-value store | More complex setup |
| **Zookeeper** | Mature, battle-tested | Java-centric, older |
| **etcd** | Kubernetes native | Requires Kubernetes |
| **Kubernetes Service Discovery** | Built-in, no extra service | Requires Kubernetes |

## When to Use

✅ **Use Eureka When**:
- Building microservices in Spring ecosystem
- Need client-side load balancing
- Dynamic service scaling required
- Running on Docker or traditional VMs

❌ **Consider Alternatives When**:
- Using Kubernetes (use native service discovery)
- Need service mesh features (use Consul/Istio)
- Non-Java microservices (use language-agnostic solution)

## Related Patterns

- **API Gateway Pattern**: Gateway uses Eureka for routing
- **Load Balancing Pattern**: Integrated with Spring Cloud LoadBalancer
- **Circuit Breaker Pattern**: Eureka provides instance health for circuit decisions
- **Health Check Pattern**: Eureka relies on health checks for registration

## References

- [Spring Cloud Netflix Eureka](https://cloud.spring.io/spring-cloud-netflix/reference/html/)
- [Netflix Eureka Wiki](https://github.com/Netflix/eureka/wiki)
- [Microservices Patterns: Service Registry](https://microservices.io/patterns/service-registry.html)

---

**Last Updated**: October 5, 2025
