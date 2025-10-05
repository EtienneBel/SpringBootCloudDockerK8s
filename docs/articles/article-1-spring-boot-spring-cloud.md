# Mastering Microservices with Spring Boot and Spring Cloud: A Comprehensive Guide (Updated 2025)

> A complete guide to building production-ready microservices with Spring Boot 3.2, Spring Cloud 2023, and modern cloud-native patterns.

---

## Introduction

In this comprehensive guide, we'll build a cloud-native e-commerce application using Spring Boot microservices architecture. You'll learn how to implement service discovery, API gateway, circuit breaker patterns, and deploy using Docker and Kubernetes.

**What You'll Learn:**
- ✅ Microservices architecture patterns
- ✅ Service discovery with Netflix Eureka
- ✅ API Gateway with Spring Cloud Gateway
- ✅ Circuit breaker pattern for fault tolerance
- ✅ Inter-service communication with OpenFeign & RestTemplate
- ✅ Centralized configuration management
- ✅ Database per service pattern

**GitHub Repository:** [Your Repository Link]

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [Microservices Breakdown](#microservices-breakdown)
4. [Setting Up Service Discovery](#setting-up-service-discovery)
5. [Building the API Gateway](#building-the-api-gateway)
6. [Implementing Circuit Breaker](#implementing-circuit-breaker)
7. [Service-to-Service Communication](#service-to-service-communication)
8. [Running the Application](#running-the-application)
9. [Testing with Swagger UI](#testing-with-swagger-ui)
10. [Next Steps](#next-steps)

---

## Architecture Overview

Our e-commerce application follows a microservices architecture with the following components:

```
┌──────────────────────────────────────────────────────────┐
│                        Client                            │
│                  (Browser/Mobile App)                    │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▼
┌───────────────────────────────────────────────────────────┐
│                   Cloud Gateway                           │
│              (Spring Cloud Gateway)                       │
│         Port 9090 - Single Entry Point                   │
│  • Routing          • Circuit Breaker                    │
│  • Load Balancing   • JWT Validation                     │
└───────────────────┬───────────────────────────────────────┘
                    │
        ┌───────────┼───────────┬──────────────┐
        ▼           ▼           ▼              ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  Product    │ │   Order     │ │  Payment    │ │   Service   │
│  Service    │ │   Service   │ │  Service    │ │  Registry   │
│  Port 8081  │ │  Port 8082  │ │  Port 8083  │ │  (Eureka)   │
│             │ │             │ │             │ │  Port 8761  │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
        │               │               │
        ▼               ▼               ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  MySQL DB   │ │  MySQL DB   │ │  MySQL DB   │
│ (Products)  │ │  (Orders)   │ │ (Payments)  │
└─────────────┘ └─────────────┘ └─────────────┘

                ┌─────────────────┐
                │  Config Server  │
                │   Port 9296     │
                └─────────────────┘
```

**Key Design Decisions:**
- **Single Entry Point:** All requests go through the API Gateway
- **Service Discovery:** Services register dynamically with Eureka
- **Database per Service:** Each microservice owns its database
- **Fault Tolerance:** Circuit breakers prevent cascading failures
- **Distributed Configuration:** Centralized config management

---

## Technology Stack

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Framework** | Spring Boot | 3.2.0 | Core framework |
| **Language** | Java | 17+ | Programming language |
| **Cloud** | Spring Cloud | 2023.0.0 | Cloud-native patterns |
| **Service Discovery** | Netflix Eureka | Latest | Service registration |
| **API Gateway** | Spring Cloud Gateway | Latest | Routing & filtering |
| **Resilience** | Resilience4j | Latest | Circuit breaker |
| **Database** | MySQL | 8.0 | Relational database |
| **Communication** | OpenFeign + RestTemplate | Latest | HTTP clients |
| **Build Tool** | Maven | 3.9+ | Dependency management |
| **API Docs** | Springdoc OpenAPI | 2.2.0 | Swagger UI |
| **Containerization** | Docker | Latest | Container platform |
| **Orchestration** | Kubernetes | 1.28+ | Container orchestration |

---

## Microservices Breakdown

### 1. Service Registry (Eureka Server)
**Purpose:** Service discovery and registration
**Port:** 8761
**Key Features:**
- Service health monitoring
- Dynamic service lookup
- Load balancer integration

### 2. Cloud Gateway
**Purpose:** API Gateway and routing
**Port:** 9090
**Key Features:**
- Single entry point for all requests
- Dynamic routing based on service discovery
- Circuit breaker integration
- JWT token validation
- CORS configuration

### 3. Config Server
**Purpose:** Centralized configuration management
**Port:** 9296
**Key Features:**
- Git-backed configuration
- Environment-specific configs
- Dynamic configuration refresh

### 4. Product Service
**Purpose:** Product catalog management
**Port:** 8081
**Database:** productDb
**Endpoints:**
- `GET /product` - List all products
- `GET /product/{id}` - Get product by ID
- `POST /product` - Create product
- `PUT /product/{id}` - Update product
- `DELETE /product/{id}` - Delete product
- `PUT /product/reduceQuantity/{id}` - Reduce stock

### 5. Order Service
**Purpose:** Order management
**Port:** 8082
**Database:** orderDb
**Endpoints:**
- `POST /order/placeOrder` - Place new order
- `GET /order/{id}` - Get order details
- `GET /order` - List all orders
- `PUT /order/{id}` - Update order
- `DELETE /order/{id}` - Delete order

### 6. Payment Service
**Purpose:** Payment processing
**Port:** 8083
**Database:** paymentDb
**Endpoints:**
- `POST /payment` - Process payment
- `GET /payment/{id}` - Get payment details
- `GET /payment/order/{orderId}` - Get payment by order

---

## Setting Up Service Discovery

### Step 1: Create Eureka Server

**pom.xml**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
</dependencies>

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

**Application Class**
```java
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
```

**application.yml**
```yaml
server:
  port: 8761

spring:
  application:
    name: SERVICE-REGISTRY

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false  # Don't register itself
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

**Access Eureka Dashboard:** http://localhost:8761

---

### Step 2: Register Microservices with Eureka

**Add Dependency to Each Service**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**Service Configuration (Product Service Example)**
```yaml
spring:
  application:
    name: PRODUCT-SERVICE

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
```

**Enable Discovery Client**
```java
@SpringBootApplication
@EnableDiscoveryClient
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```

**Repeat for OrderService and PaymentService.**

---

## Building the API Gateway

### Step 1: Create Gateway Service

**pom.xml**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
    </dependency>
</dependencies>
```

**Application Class**
```java
@SpringBootApplication
@EnableDiscoveryClient
public class CloudGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(CloudGatewayApplication.class, args);
    }
}
```

### Step 2: Configure Routes

**application.yml**
```yaml
server:
  port: 9090

spring:
  application:
    name: API-GATEWAY
  cloud:
    gateway:
      routes:
        # Product Service Routes
        - id: PRODUCT-SERVICE
          uri: lb://PRODUCT-SERVICE  # lb = load balanced
          predicates:
            - Path=/api/products/**
          filters:
            - name: CircuitBreaker
              args:
                name: PRODUCT-SERVICE
                fallbackuri: forward:/productServiceFallback

        # Order Service Routes
        - id: ORDER-SERVICE
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/orders/**
          filters:
            - name: CircuitBreaker
              args:
                name: ORDER-SERVICE
                fallbackuri: forward:/orderServiceFallback

        # Payment Service Routes
        - id: PAYMENT-SERVICE
          uri: lb://PAYMENT-SERVICE
          predicates:
            - Path=/api/payments/**
          filters:
            - name: CircuitBreaker
              args:
                name: PAYMENT-SERVICE
                fallbackuri: forward:/paymentServiceFallback

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka
    register-with-eureka: true
    fetch-registry: true
```

**How Routes Work:**
1. Client sends request: `http://localhost:9090/api/products/123`
2. Gateway matches route by predicate: `Path=/api/products/**`
3. Gateway resolves `lb://PRODUCT-SERVICE` via Eureka
4. Gateway forwards to: `http://product-service-instance/products/123`
5. Circuit breaker monitors the call
6. Returns response to client

---

## Implementing Circuit Breaker

Circuit breaker prevents cascading failures when a service is down or slow.

### Step 1: Configure Resilience4j

**application.yml (Cloud Gateway)**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      PRODUCT-SERVICE:
        sliding-window-size: 10              # Monitor last 10 requests
        failure-rate-threshold: 50           # Open if 50% fail
        wait-duration-in-open-state: 10000   # Wait 10s before retry
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
      ORDER-SERVICE:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
      PAYMENT-SERVICE:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000

  timelimiter:
    instances:
      PRODUCT-SERVICE:
        timeout-duration: 10s
      ORDER-SERVICE:
        timeout-duration: 10s
      PAYMENT-SERVICE:
        timeout-duration: 10s
```

### Step 2: Create Fallback Controller

**FallbackController.java**
```java
@RestController
public class FallbackController {

    @GetMapping("/productServiceFallback")
    public String productServiceFallback() {
        return "Product Service is temporarily unavailable. Please try again later.";
    }

    @GetMapping("/orderServiceFallback")
    public String orderServiceFallback() {
        return "Order Service is temporarily unavailable. Please try again later.";
    }

    @GetMapping("/paymentServiceFallback")
    public String paymentServiceFallback() {
        return "Payment Service is temporarily unavailable. Please try again later.";
    }
}
```

### Circuit Breaker States

```
┌──────────────┐
│    CLOSED    │  ← Normal operation
│  (Healthy)   │
└──────┬───────┘
       │
       │ Failure rate > 50%
       ▼
┌──────────────┐
│     OPEN     │  ← Rejects all requests
│   (Failed)   │     Returns fallback
└──────┬───────┘
       │
       │ After wait duration
       ▼
┌──────────────┐
│ HALF-OPEN    │  ← Allows 3 test requests
│  (Testing)   │
└──────┬───────┘
       │
       ├─ Success → CLOSED
       └─ Failure → OPEN
```

---

## Service-to-Service Communication

Our application uses **two approaches** for inter-service communication:

### 1. OpenFeign (Declarative - for Write Operations)

**Add Dependency**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**Enable Feign Clients**
```java
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

**Create Feign Client Interface**
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

**Usage in Service**
```java
@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductService productService;  // Feign client

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Placing order: {}", orderRequest);

        // Call Product Service to reduce quantity
        productService.reduceQuantity(
            orderRequest.getProductId(),
            orderRequest.getQuantity()
        );

        // Save order...
        return order.getId();
    }
}
```

### 2. RestTemplate (Imperative - for Read Operations)

**Configure RestTemplate with Load Balancing**
```java
@SpringBootApplication
public class OrderServiceApplication {

    @Bean
    @LoadBalanced  // Enables Eureka service discovery
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**Usage in Service**
```java
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        // Fetch product details
        ProductResponse product = restTemplate.getForObject(
            "http://PRODUCT-SERVICE/product/" + order.getProductId(),
            ProductResponse.class
        );

        // Fetch payment details
        PaymentResponse payment = restTemplate.getForObject(
            "http://PAYMENT-SERVICE/payment/order/" + order.getId(),
            PaymentResponse.class
        );

        // Build response
        return OrderResponse.builder()
            .orderId(order.getId())
            .productDetails(product)
            .paymentDetails(payment)
            .build();
    }
}
```

**Why Two Approaches?**

| Feature | OpenFeign | RestTemplate |
|---------|-----------|--------------|
| **Syntax** | Declarative (interface) | Imperative (code) |
| **Use Case** | Write operations | Read operations |
| **Type Safety** | ✅ Compile-time | ❌ Runtime |
| **Boilerplate** | Low | Higher |
| **Debugging** | Harder | Easier |
| **Best For** | POST, PUT, DELETE | GET, complex logic |

---

## Running the Application

### Option 1: Run Locally with Maven

**Step 1: Start Infrastructure Services**
```bash
# Terminal 1 - Service Registry
cd ServiceRegistry
mvn spring-boot:run

# Terminal 2 - Config Server
cd ConfigServer
mvn spring-boot:run
```

**Step 2: Start Business Services**
```bash
# Terminal 3 - Product Service
cd ProductService
mvn spring-boot:run

# Terminal 4 - Order Service
cd OrderService
mvn spring-boot:run

# Terminal 5 - Payment Service
cd PaymentService
mvn spring-boot:run
```

**Step 3: Start API Gateway**
```bash
# Terminal 6 - Cloud Gateway
cd CloudGateway
mvn spring-boot:run
```

### Option 2: Run with Docker Compose

**Prerequisites:**
- Docker installed
- Docker Compose installed

**Start All Services**
```bash
docker-compose -f docker-compose.dev.yml up -d
```

**Check Service Status**
```bash
docker-compose -f docker-compose.dev.yml ps
```

**View Logs**
```bash
# All services
docker-compose -f docker-compose.dev.yml logs -f

# Specific service
docker-compose -f docker-compose.dev.yml logs -f product-service
```

**Stop All Services**
```bash
docker-compose -f docker-compose.dev.yml down
```

---

## Testing with Swagger UI

Each service has Swagger UI enabled for interactive API testing.

### Access Swagger UI

**Option 1: Via Gateway (Aggregated)**
- URL: http://localhost:9090/swagger-ui.html
- Provides access to all service APIs in one place

**Option 2: Direct Service Access**
- Product Service: http://localhost:8081/swagger-ui.html
- Order Service: http://localhost:8082/swagger-ui.html
- Payment Service: http://localhost:8083/swagger-ui.html

### Test Flow Example

**1. Create a Product**
```bash
POST http://localhost:9090/api/products
{
  "productName": "iPhone 15",
  "price": 999.99,
  "quantity": 100
}
```

**2. Place an Order**
```bash
POST http://localhost:9090/api/orders/placeOrder
{
  "productId": 1,
  "quantity": 2,
  "totalAmount": 1999.98,
  "paymentMode": "CREDIT_CARD"
}
```

**3. Get Order Details**
```bash
GET http://localhost:9090/api/orders/1
```

**Response:**
```json
{
  "orderId": 1,
  "orderStatus": "PLACED",
  "orderDate": "2025-01-15T10:30:00Z",
  "amount": 1999.98,
  "productDetails": {
    "productId": 1,
    "productName": "iPhone 15"
  },
  "paymentDetails": {
    "paymentId": 1,
    "status": "SUCCESS",
    "paymentMode": "CREDIT_CARD",
    "paymentDate": "2025-01-15T10:30:05Z"
  }
}
```

---

## Monitoring & Health Checks

### Eureka Dashboard
**URL:** http://localhost:8761

**What to Check:**
- All services registered and UP
- Instance count for each service
- Last heartbeat time

### Actuator Endpoints

All services expose health and metrics endpoints:

```bash
# Health check
curl http://localhost:8081/actuator/health

# Service info
curl http://localhost:8081/actuator/info

# Metrics
curl http://localhost:8081/actuator/metrics
```

---

## Best Practices Implemented

✅ **Database per Service** - Each microservice owns its database
✅ **API Gateway Pattern** - Single entry point for all clients
✅ **Service Discovery** - Dynamic service registration with Eureka
✅ **Circuit Breaker** - Fault tolerance with Resilience4j
✅ **Load Balancing** - Client-side load balancing via Eureka
✅ **Health Checks** - Spring Boot Actuator endpoints
✅ **API Documentation** - Swagger UI for all services
✅ **Centralized Configuration** - Spring Cloud Config Server
✅ **Container-Ready** - Docker and Kubernetes support

---

## Common Issues & Solutions

### Issue 1: Service Not Registering with Eureka
**Solution:**
- Check `eureka.client.serviceUrl.defaultZone` is correct
- Ensure Eureka Server is running
- Verify network connectivity

### Issue 2: Gateway Returns 503 Service Unavailable
**Solution:**
- Check service is registered with Eureka
- Verify route configuration in gateway
- Check circuit breaker state

### Issue 3: Feign Client Not Found
**Solution:**
- Add `@EnableFeignClients` to main application class
- Verify Feign interface package is scanned
- Check dependency is in pom.xml

---

## Next Steps

In Part 2, we'll cover:
- 🔐 **Security with OAuth2 and Auth0**
- 🔑 **JWT token validation**
- 🛡️ **Service-to-service authentication**
- 🌐 **CORS configuration for frontends**

In Part 3, we'll explore:
- 🐳 **Docker containerization**
- ☸️ **Kubernetes deployment**
- 🚀 **CI/CD pipeline setup**
- 📊 **Monitoring with Prometheus & Grafana**

---

## Conclusion

You've successfully built a production-ready microservices application with:
- ✅ 6 microservices with clear responsibilities
- ✅ Service discovery and registration
- ✅ API Gateway with routing and circuit breaker
- ✅ Inter-service communication with OpenFeign & RestTemplate
- ✅ Fault tolerance and resilience
- ✅ Interactive API documentation

The complete source code is available on GitHub: [Your Repository Link]

**Questions? Comments?** Leave them below or reach out on LinkedIn!

---

**Author:** Etienne Belem Gnegre
**Date:** January 2025
**Tags:** #SpringBoot #Microservices #SpringCloud #Java #CloudNative #Netflix #Eureka #APIGateway

---

## Additional Resources

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Netflix Eureka](https://github.com/Netflix/eureka)
- [Project Repository](https://github.com/your-username/spring-boot-microservices)
