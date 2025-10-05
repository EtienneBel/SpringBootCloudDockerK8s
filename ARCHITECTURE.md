# Microservices Architecture Documentation

> Complete technical documentation of features, patterns, and tools used in this Spring Boot microservices application.

---

## Table of Contents

- [Microservices Architecture Patterns](#microservices-architecture-patterns)
- [Security Features](#security-features)
- [API Documentation](#api-documentation)
- [Containerization & Orchestration](#containerization--orchestration)
- [Development Tools](#development-tools)
- [Observability & Monitoring](#observability--monitoring)
- [Data Management](#data-management)
- [Testing](#testing)
- [Communication Protocols](#communication-protocols)
- [Deployment Strategies](#deployment-strategies)
- [Technology Stack](#technology-stack)
- [Service Breakdown](#service-breakdown)
- [Best Practices Implemented](#best-practices-implemented)
- [Future Enhancements](#future-enhancements)

---

## Microservices Architecture Patterns

### 1. Service Discovery Pattern

**Pattern**: Client-Side Service Discovery
**Tool**: Netflix Eureka Server
**Port**: 8761
**Location**: `ServiceRegistry/`

**How it works:**
- All microservices register with Eureka Server on startup
- Services send periodic heartbeats to maintain registration
- Clients query Eureka to discover available service instances
- Automatic deregistration when service goes down

**Configuration:**
```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://serviceregistry:8761/eureka
```

**Benefits:**
- Dynamic service discovery
- No hardcoded service URLs
- Automatic load balancing
- Health monitoring

---

### 2. API Gateway Pattern

**Pattern**: Single Entry Point, Gateway Routing
**Tool**: Spring Cloud Gateway (WebFlux-based, non-blocking)
**Port**: 9090
**Location**: `CloudGateway/`

**Features:**
- Dynamic routing based on service discovery
- Client-side load balancing
- Circuit breaker integration
- JWT token validation at gateway level
- CORS handling for frontend applications
- Request/response transformation
- Rate limiting (ready to implement)

**Routing Example:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: PRODUCT-SERVICE
          uri: lb://PRODUCT-SERVICE  # Load balanced
          predicates:
            - Path=/product/**
          filters:
            - StripPrefix=1  # Remove /product prefix
            - name: CircuitBreaker
              args:
                name: PRODUCT-SERVICE
                fallbackuri: forward:/productServiceFallback
```

**Benefits:**
- Single entry point for all clients
- Centralized authentication/authorization
- Protocol translation
- Request aggregation
- Simplified client code

---

### 3. Externalized Configuration Pattern

**Pattern**: Centralized Configuration Management
**Tool**: Spring Cloud Config Server
**Port**: 9296
**Location**: `ConfigServer/`

**How it works:**
- Configuration stored in Git repository
- Services fetch configuration on startup
- Supports environment-specific configs (dev, prod)
- Dynamic configuration refresh without restart

**Configuration:**
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-org/config-repo
          clone-on-start: true
```

**Benefits:**
- Centralized configuration management
- Version-controlled configurations
- Environment separation
- No configuration in Docker images
- Runtime configuration updates

---

### 4. Circuit Breaker Pattern

**Pattern**: Fault Tolerance, Resilience
**Tool**: Resilience4j (Reactor-based)
**Location**:
- Gateway routes: `CloudGateway/src/main/resources/application-dev.yml`
- Fallback controller: `CloudGateway/controller/FallbackController.java`

**Implementation:**
```yaml
filters:
  - name: CircuitBreaker
    args:
      name: ORDER-SERVICE
      fallbackuri: forward:/orderServiceFallback
```

**Fallback Response:**
```java
@GetMapping("/orderServiceFallback")
public String orderServiceFallback() {
    return "OrderService is down";
}
```

**Benefits:**
- Prevent cascading failures
- Graceful degradation
- Automatic recovery
- Fast failure detection
- User-friendly error messages

**Circuit States:**
- **Closed**: Normal operation, requests pass through
- **Open**: Service failing, requests immediately fail with fallback
- **Half-Open**: Testing if service recovered

---

### 5. Database per Service Pattern

**Pattern**: Each microservice owns its database schema
**Tool**: MySQL 8.0 (Production), H2 (Testing)

**Database Mapping:**

| Service | Database | Port | Purpose |
|---------|----------|------|---------|
| ProductService | productDb | 3306 | Product catalog, inventory |
| OrderService | orderDb | 3307 | Orders, order items |
| PaymentService | paymentDb | 3308 | Payment transactions |

**Benefits:**
- Loose coupling between services
- Independent scaling
- Technology diversity (can use different DB types)
- Simplified schema changes
- No shared database contention

**Challenges Addressed:**
- Data consistency: Eventual consistency model
- Distributed transactions: Saga pattern (ready to implement)
- Data duplication: Acceptable tradeoff for autonomy

---

### 6. Service-to-Service Communication Pattern

**Pattern**: Synchronous REST Communication with OAuth2
**Tools**:
- RestTemplate with OAuth2 interceptors
- OpenFeign declarative HTTP client

**Location**: `OrderService/external/`

**Implementation:**

**RestTemplate with OAuth2 Interceptor:**
```java
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder,
                                  OAuth2AuthorizedClientManager clientManager) {
    RestTemplate restTemplate = builder.build();

    restTemplate.getInterceptors().add(
        new RestTemplateInterceptor(clientManager)
    );

    return restTemplate;
}
```

**OpenFeign Client:**
```java
@FeignClient(name = "PRODUCT-SERVICE", path = "/product")
public interface ProductService {
    @GetMapping("/{id}")
    ProductResponse getProductById(@PathVariable("id") long productId);
}
```

**OAuth2 Token Injection:**
```java
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) {
        OAuth2AuthorizedClient client = clientManager.authorize(
            OAuth2AuthorizeRequest.withClientRegistrationId("internal-client")
                .principal("internal")
                .build()
        );

        String token = client.getAccessToken().getTokenValue();
        request.getHeaders().add("Authorization", "Bearer " + token);

        return execution.execute(request, body);
    }
}
```

**Benefits:**
- Automatic OAuth2 token management
- No manual token handling in business logic
- Automatic token refresh
- Declarative API clients (Feign)
- Load balancing through Eureka integration

---

### 7. Authentication & Authorization Pattern

**Pattern**: API Gateway Authentication with OAuth2/OIDC
**Provider**: Auth0
**Protocols**: OAuth2, OpenID Connect (OIDC)
**Token Type**: JWT (JSON Web Tokens)

#### Architecture Overview

```
┌─────────────────┐
│   Frontend      │
│ (React/Vue)     │
└────────┬────────┘
         │
         │ (1) Login Request
         ▼
┌─────────────────────────┐
│   CloudGateway          │
│   - Regular Web App     │
│   - Authorization Code  │
└────────┬────────────────┘
         │
         │ (2) Redirect to Auth0
         ▼
┌─────────────────────────┐
│   Auth0                 │
│   - User Authentication │
│   - Token Issuance      │
└────────┬────────────────┘
         │
         │ (3) Tokens (Access + Refresh)
         ▼
┌─────────────────────────┐
│   CloudGateway          │
│   - JWT Validation      │
│   - Routing             │
└────────┬────────────────┘
         │
         │ (4) Validated Request
         ▼
┌─────────────────────────┐
│   Backend Services      │
│   - JWT Validation      │
│   - Business Logic      │
└─────────────────────────┘

Service-to-Service:
┌─────────────────────────┐
│   OrderService          │
│   - M2M Application     │
│   - Client Credentials  │
└────────┬────────────────┘
         │
         │ Auto-inject OAuth2 token
         ▼
┌─────────────────────────┐
│   ProductService        │
│   PaymentService        │
└─────────────────────────┘
```

#### Auth0 Application Types

| Application Type | Used By | Grant Type | Purpose |
|------------------|---------|------------|---------|
| **Regular Web Application** | CloudGateway | Authorization Code + Refresh Token | Frontend user login (browser-based) |
| **Machine-to-Machine (M2M)** | OrderService, PaymentService | Client Credentials | Backend service-to-service API calls |

#### OAuth2 Flows Implemented

**Flow 1: Authorization Code Flow (User Login)**
- User clicks "Login" → Redirects to Auth0
- User authenticates with Auth0 (email/password, social login, etc.)
- Auth0 redirects back with authorization code
- CloudGateway exchanges code for access token, refresh token, ID token
- Tokens returned to frontend

**Flow 2: Client Credentials Flow (Service-to-Service)**
- OrderService needs to call ProductService
- RestTemplate interceptor automatically obtains token from Auth0
- Token added to `Authorization: Bearer <token>` header
- Token cached and auto-refreshed when expired

#### JWT Token Structure

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "key-id"
  },
  "payload": {
    "iss": "https://dev-5nw6367bfpr277ec.us.auth0.com/",
    "sub": "auth0|user-id",
    "aud": "http://springboot-microservices-api",
    "exp": 1759750871,
    "iat": 1759664471,
    "scope": "openid profile email offline_access",
    "email": "user@example.com"
  },
  "signature": "..."
}
```

#### Environment Variables (Option B - Two Separate Apps)

```bash
# CloudGateway - Regular Web Application (user login)
AUTH0_CLIENT_ID=<regular_web_app_client_id>
AUTH0_CLIENT_SECRET=<regular_web_app_client_secret>

# Backend Services - Machine-to-Machine (service-to-service)
AUTH0_M2M_CLIENT_ID=<m2m_client_id>
AUTH0_M2M_CLIENT_SECRET=<m2m_client_secret>

# Shared Configuration
AUTH0_AUDIENCE=http://springboot-microservices-api
AUTH0_ISSUER_URI=https://dev-5nw6367bfpr277ec.us.auth0.com/
```

#### Security Configuration per Service

**CloudGateway (Resource Server + OAuth2 Client):**
```yaml
spring:
  security:
    oauth2:
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
          audiences: ${AUTH0_AUDIENCE}
      client:
        registration:
          auth0:
            client-id: ${AUTH0_CLIENT_ID}
            client-secret: ${AUTH0_CLIENT_SECRET}
            scope: openid,profile,email,offline_access
```

**OrderService (Resource Server + OAuth2 Client for service calls):**
```yaml
spring:
  security:
    oauth2:
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
      client:
        registration:
          internal-client:
            provider: auth0
            authorization-grant-type: client_credentials
            client-id: ${AUTH0_M2M_CLIENT_ID}
            client-secret: ${AUTH0_M2M_CLIENT_SECRET}
```

**ProductService, PaymentService (Resource Server only):**
```yaml
spring:
  security:
    oauth2:
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
```

#### Benefits
- Industry-standard authentication
- Stateless authentication (JWT)
- Automatic token refresh
- Centralized user management (Auth0)
- Multi-factor authentication support
- Social login support (Google, Facebook, etc.)
- Separation of concerns (authentication vs authorization)

---

### 8. Load Balancing Pattern

**Pattern**: Client-Side Load Balancing
**Tool**: Spring Cloud LoadBalancer
**Integration**: Eureka Service Discovery

**How it works:**
- Load balancer integrated with Eureka
- Gateway uses `lb://SERVICE-NAME` URI scheme
- Automatically distributes requests across available instances
- Health-based instance selection

**Configuration:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: PRODUCT-SERVICE
          uri: lb://PRODUCT-SERVICE  # Load balanced via Eureka
```

**Load Balancing Strategies:**
- **Round Robin** (default)
- **Random**
- **Weighted Response Time**

**Benefits:**
- No single point of failure
- Horizontal scaling
- Better resource utilization
- Automatic failover

---

## Security Features

### Security Layers

| Layer | Implementation | Purpose |
|-------|----------------|---------|
| **Gateway Authentication** | OAuth2 + JWT validation | Single authentication point |
| **Service Authorization** | JWT validation per service | Defense in depth |
| **OAuth2 Token Injection** | RestTemplate/Feign interceptors | Automated service-to-service auth |
| **CORS Protection** | WebFlux CORS filter | Cross-origin request security |
| **Environment Variables** | Externalized secrets | No hardcoded credentials |
| **Scope-Based Authorization** | Spring Security expressions | Fine-grained access control |

### CORS Configuration

**Location**: `CloudGateway/config/CorsConfig.java`

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Specific allowed origins (no wildcard with credentials)
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // React
            "http://localhost:4200",  // Angular
            "http://localhost:8080",  // Vue
            "http://localhost:5173"   // Vite
        ));

        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
```

### Security Best Practices Implemented

✅ **No Hardcoded Credentials**: All secrets in environment variables
✅ **Separate Auth Applications**: Regular Web App (user) + M2M App (services)
✅ **JWT Validation**: Every service validates tokens independently
✅ **Automatic Token Refresh**: Refresh token support enabled
✅ **HTTPS Ready**: Configuration supports HTTPS (enable in production)
✅ **Scope-Based Authorization**: Fine-grained permissions
✅ **Defense in Depth**: Multiple security layers
✅ **Secrets Management**: `.env` in `.gitignore`

---

## API Documentation

### OpenAPI / Swagger 3.0

**Tool**: Springdoc OpenAPI
**Version**: 2.2.0

#### Features

- **Interactive API Testing**: Swagger UI for all endpoints
- **Centralized Documentation**: Aggregated at API Gateway
- **Bearer Token Support**: Built-in authentication testing
- **Schema Annotations**: Detailed request/response documentation
- **Auto-Generated**: From controller annotations

#### Endpoints

| Service | Swagger UI URL | OpenAPI JSON |
|---------|----------------|--------------|
| **Centralized (All Services)** | http://localhost:9090/swagger-ui.html | Multiple sources |
| Cloud Gateway | http://localhost:9090/swagger-ui.html | http://localhost:9090/api-docs |
| Product Service | http://localhost:8081/swagger-ui.html | http://localhost:8081/api-docs |
| Order Service | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs |
| Payment Service | http://localhost:8083/swagger-ui.html | http://localhost:8083/api-docs |

#### Centralized Swagger Configuration

**Location**: `CloudGateway/src/main/resources/application-dev.yml`

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    urls:
      - name: Cloud Gateway
        url: /api-docs
      - name: Product Service
        url: /product/api-docs
      - name: Order Service
        url: /order/api-docs
      - name: Payment Service
        url: /payment/api-docs
```

#### Documentation Annotations Example

```java
@RestController
@RequestMapping("/authenticate")
@Tag(name = "Authentication", description = "OAuth2/OIDC authentication endpoints")
public class AuthenticationController {

    @Operation(
        summary = "OAuth2 Login",
        description = "Initiates OAuth2/OIDC login flow with Auth0"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully authenticated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthenticationResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
        )
    })
    @GetMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(...) {
        // Implementation
    }
}
```

#### Model Schema Annotations

```java
@Data
@Schema(description = "Authentication response containing user information and OAuth2 tokens")
public class AuthenticationResponse {

    @Schema(
        description = "User identifier (email address from Auth0)",
        example = "user@example.com",
        required = true
    )
    private String userId;

    @Schema(
        description = "OAuth2 access token (JWT) for API authentication",
        example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = true
    )
    private String accessToken;
}
```

---

## Containerization & Orchestration

### Docker

#### Dockerfile Variants

**Production Dockerfile (`Dockerfile.prod`)**:
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Development Dockerfile (`Dockerfile.dev`)**:
```dockerfile
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app

# Copy source for hot reload
COPY src ./src
COPY pom.xml .

# Enable remote debugging on port 5005
ENTRYPOINT ["mvn", "spring-boot:run", \
    "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"]
```

**Features**:
- Hot reload in development (no rebuild needed)
- Remote debugging support
- Volume mounts for source code
- Shared Maven repository cache

#### Docker Compose

**Production Stack (`docker-compose.yml`)**:
- Uses production Dockerfiles
- Optimized images
- No volume mounts
- Production-ready configuration

**Development Stack (`docker-compose.dev.yml`)**:
- Hot reload with volume mounts
- Debug ports exposed
- Development databases
- Faster iteration cycle

**Key Features**:
- Health checks for all services
- Dependency management (depends_on with conditions)
- Environment variable injection from `.env`
- Shared network for service communication
- Persistent volumes for databases

#### Build Tools

**Jib Maven Plugin**: Containerless Docker image builds

```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <configuration>
        <from>
            <image>openjdk@sha256:528707081fdb9562eb819128a9f85ae7fe000e2fbaeaf9f87662e7b3f38cb7d8</image>
        </from>
        <to>
            <image>registry.hub.docker.com/etiennebel/cloudgateway</image>
        </to>
    </configuration>
</plugin>
```

**Benefits**:
- No Docker daemon required
- Faster builds (layer caching)
- Reproducible builds
- Smaller images

### Kubernetes (Ready for Deployment)

**Location**: `/k8s/` directory

#### Kubernetes Resources

| Resource Type | File | Purpose |
|---------------|------|---------|
| **StatefulSet** | service-registry-statefulset.yml | Eureka Server with persistent identity |
| **Deployment** | config-server-deployment.yml | Config Server |
| **Deployment** | cloud-gateway-deployment.yml | API Gateway |
| **Deployment** | product-service-deployment.yml | Product Service |
| **Deployment** | order-service-deployment.yml | Order Service |
| **Deployment** | payment-service-deployment.yml | Payment Service |
| **Deployment** | mysql-deployment.yml | MySQL databases |
| **ConfigMap** | config-maps.yml | Application configuration |

#### Kubernetes Features Ready

- **Service Discovery**: Headless services for Eureka
- **Load Balancing**: Kubernetes Services
- **Health Checks**: Liveness and readiness probes
- **ConfigMaps**: External configuration
- **Secrets**: Sensitive data (add manually)
- **Horizontal Pod Autoscaling**: Ready to enable
- **Ingress**: Ready to configure

---

## Development Tools

| Tool | Purpose | Implementation | Location |
|------|---------|----------------|----------|
| **Spring Boot DevTools** | Hot reload, auto-restart | All services | Runtime dependency |
| **Lombok** | Reduce boilerplate | `@Data`, `@Builder`, `@Log4j2` | All models/controllers |
| **Maven** | Build, dependency management | Multi-module project | `pom.xml` files |
| **H2 Database** | In-memory testing database | Test scope | All services |
| **WireMock** | HTTP mocking for integration tests | Test scope | OrderService |
| **Dev Containers** | VS Code/Cursor containerized dev | `.devcontainer/devcontainer.json` | All services |

### Dev Containers Configuration

**Location**: Each service has `.devcontainer/devcontainer.json`

**Features**:
- Consistent development environment
- Automatic port forwarding
- Extensions pre-installed
- Docker-in-Docker support
- Remote debugging configured

### Lombok Annotations Used

```java
@Data                    // Getters, setters, toString, equals, hashCode
@AllArgsConstructor      // Constructor with all fields
@NoArgsConstructor       // Default constructor
@Builder                 // Builder pattern
@Log4j2                  // Logger field
@RequiredArgsConstructor // Constructor for final fields
```

---

## Observability & Monitoring

### Spring Boot Actuator

**Endpoints Exposed**:

| Endpoint | Purpose | URL |
|----------|---------|-----|
| **Health** | Service health status | `/actuator/health` |
| **Info** | Application information | `/actuator/info` |
| **Metrics** | Application metrics | `/actuator/metrics` |
| **Env** | Environment properties | `/actuator/env` |

**Health Check Integration**:
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
  interval: 10s
  timeout: 5s
  retries: 10
```

### Logging

**Tool**: Log4j2 (via Lombok `@Log4j2`)

**Usage**:
```java
@Log4j2
@RestController
public class OrderController {
    public Order createOrder(Order order) {
        log.info("Creating order: {}", order);
        log.debug("Order details: {}", order.getItems());
        log.error("Failed to process order", exception);
    }
}
```

**Log Levels**: TRACE, DEBUG, INFO, WARN, ERROR

### Distributed Tracing (Ready to Enable)

**Tools** (Currently commented out in `OrderService/pom.xml`):
- Micrometer Observation
- Micrometer Tracing (Brave)
- Zipkin Reporter

```xml
<!-- Uncomment to enable distributed tracing -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-observation</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

**What you'll get when enabled**:
- Request tracing across services
- Latency analysis
- Dependency graphs
- Performance bottleneck identification
- Zipkin UI for visualization

---

## Data Management

### ORM & Persistence

**Tool**: Spring Data JPA
**Provider**: Hibernate
**Database Driver**: MySQL Connector/J

**Architecture**:
```
Controller → Service → Repository → Entity → Database
```

**Example Entity**:
```java
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private BigDecimal price;

    private Integer quantity;
}
```

**Example Repository**:
```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContaining(String name);

    @Query("SELECT p FROM Product p WHERE p.price < :maxPrice")
    List<Product> findAffordableProducts(@Param("maxPrice") BigDecimal maxPrice);
}
```

### Database Configuration

**Development (H2)**:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

**Production (MySQL)**:
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3306/productDb
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

### Database Migration (Recommended - Not Yet Implemented)

**Suggested Tools**:
- **Flyway**: Version-controlled SQL migrations
- **Liquibase**: XML/YAML-based migrations

**Benefits**:
- Version-controlled schema changes
- Repeatable deployments
- Rollback capability
- Team collaboration

---

## Testing

### Testing Pyramid

```
         /\
        /  \  Unit Tests
       /____\
      /      \  Integration Tests
     /________\
    /          \  End-to-End Tests
   /____________\
```

### Testing Tools & Frameworks

| Tool | Type | Scope | Purpose |
|------|------|-------|---------|
| **JUnit 5** | Unit testing | All services | Test framework |
| **Spring Boot Test** | Integration testing | All services | Spring context tests |
| **Mockito** | Mocking | All services | Mock dependencies |
| **AssertJ** | Assertions | All services | Fluent assertions |
| **RestAssured** | API testing | Services | REST API tests |
| **WireMock** | HTTP mocking | OrderService | Mock external services |
| **H2 Database** | In-memory DB | All services | Test database |
| **Reactor Test** | Reactive testing | CloudGateway | WebFlux testing |
| **Spring Security Test** | Security testing | All services | Auth tests |

### Test Configuration Example

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void shouldCreateOrder() throws Exception {
        // Given
        OrderRequest request = OrderRequest.builder()
            .productId(1L)
            .quantity(2)
            .build();

        when(productService.getProductById(1L))
            .thenReturn(new Product(1L, "Test Product", 100.0, 10));

        // When & Then
        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").exists());
    }
}
```

### WireMock Example (External Service Mocking)

```java
@Test
void shouldHandleProductServiceFailure() {
    // Setup WireMock stub
    stubFor(get(urlEqualTo("/product/1"))
        .willReturn(aResponse()
            .withStatus(500)
            .withBody("Internal Server Error")));

    // Test OrderService handles failure gracefully
    assertThatThrownBy(() -> orderService.createOrder(request))
        .isInstanceOf(ServiceException.class)
        .hasMessageContaining("Product service unavailable");
}
```

---

## Communication Protocols

### HTTP/REST

**Default Protocol**: HTTP/1.1
**Framework**: Spring MVC (services), Spring WebFlux (gateway)

**RESTful Endpoints**:
- `GET /products` - List products
- `GET /products/{id}` - Get product by ID
- `POST /orders` - Create order
- `PUT /products/{id}` - Update product
- `DELETE /products/{id}` - Delete product

**Content Type**: `application/json`

### WebFlux (Reactive Programming)

**Used In**: CloudGateway
**Benefits**:
- Non-blocking I/O
- Better resource utilization
- Handles high concurrency
- Backpressure support

**Example**:
```java
@GetMapping("/products")
public Flux<Product> getAllProducts() {
    return productRepository.findAll();
}

@GetMapping("/products/{id}")
public Mono<Product> getProductById(@PathVariable Long id) {
    return productRepository.findById(id);
}
```

### Declarative REST Clients

**OpenFeign**:
```java
@FeignClient(name = "PRODUCT-SERVICE", path = "/product")
public interface ProductService {
    @GetMapping("/{id}")
    ProductResponse getProductById(@PathVariable Long id);

    @PostMapping
    ProductResponse createProduct(@RequestBody ProductRequest request);
}
```

**RestTemplate** (with OAuth2 interceptor):
```java
@Autowired
private RestTemplate restTemplate;

public Product getProduct(Long id) {
    return restTemplate.getForObject(
        "http://PRODUCT-SERVICE/product/" + id,
        Product.class
    );
}
```

---

## Deployment Strategies

### Environment Profiles

**Spring Profiles**:
- `dev` - Development environment
- `prod` - Production environment
- `test` - Testing environment

**Profile Activation**:
```yaml
# Via environment variable
SPRING_PROFILES_ACTIVE=dev

# Via application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

**Profile-Specific Configuration**:
- `application.yml` - Common config
- `application-dev.yml` - Development overrides
- `application-prod.yml` - Production overrides

### Volume Mounts (Development)

**Development Benefits**:
```yaml
volumes:
  - ./ProductService/src:/app/src        # Live code changes
  - ./ProductService/target:/app/target  # Compiled classes
  - maven-repo:/root/.m2                 # Shared Maven cache
```

**Hot Reload Process**:
1. Edit source file locally
2. Spring Boot DevTools detects change
3. Application auto-restarts (fast)
4. No Docker rebuild needed

### Environment Variable Injection

**From `.env` file**:
```bash
# .env
AUTH0_CLIENT_ID=your_client_id
AUTH0_CLIENT_SECRET=your_client_secret
```

**Loaded by Docker Compose**:
```yaml
services:
  cloudgateway:
    environment:
      - AUTH0_CLIENT_ID=${AUTH0_CLIENT_ID}
      - AUTH0_CLIENT_SECRET=${AUTH0_CLIENT_SECRET}
```

**Used in Spring**:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          auth0:
            client-id: ${AUTH0_CLIENT_ID}
```

---

## Technology Stack

### Core Technologies

| Component | Technology | Version |
|-----------|------------|---------|
| **Language** | Java | 17 |
| **Framework** | Spring Boot | 3.2.x |
| **Cloud Framework** | Spring Cloud | 2023.0.0 |
| **Build Tool** | Maven | 3.9+ |
| **Package Manager** | Maven Central | - |

### Spring Boot Starters

| Starter | Purpose |
|---------|---------|
| spring-boot-starter-web | RESTful web services (MVC) |
| spring-boot-starter-webflux | Reactive web (Gateway) |
| spring-boot-starter-data-jpa | Database access with JPA |
| spring-boot-starter-security | Security framework |
| spring-boot-starter-oauth2-resource-server | JWT validation |
| spring-boot-starter-oauth2-client | OAuth2 client |
| spring-boot-starter-actuator | Monitoring endpoints |
| spring-boot-starter-test | Testing framework |
| spring-boot-devtools | Hot reload, dev tools |

### Spring Cloud Components

| Component | Library | Purpose |
|-----------|---------|---------|
| **Config Server** | spring-cloud-config-server | Centralized configuration |
| **Config Client** | spring-cloud-starter-config | Config client |
| **Service Registry** | spring-cloud-starter-netflix-eureka-server | Eureka Server |
| **Discovery Client** | spring-cloud-starter-netflix-eureka-client | Eureka Client |
| **API Gateway** | spring-cloud-starter-gateway | Routing, filtering |
| **Circuit Breaker** | spring-cloud-starter-circuitbreaker-reactor-resilience4j | Fault tolerance |
| **Load Balancer** | spring-cloud-starter-loadbalancer | Client-side LB |
| **OpenFeign** | spring-cloud-starter-openfeign | Declarative REST client |

### Infrastructure

| Component | Technology | Version |
|-----------|------------|---------|
| **Containerization** | Docker | 20.10+ |
| **Orchestration** | Docker Compose | 2.x |
| **Kubernetes** | Kubernetes | 1.25+ (ready) |
| **Database (Prod)** | MySQL | 8.0 |
| **Database (Test)** | H2 | In-memory |
| **Authentication** | Auth0 | Cloud |
| **API Documentation** | Springdoc OpenAPI | 2.2.0 |
| **Image Builder** | Jib | 3.x |

### Additional Libraries

| Library | Purpose |
|---------|---------|
| **Lombok** | Reduce boilerplate code |
| **WireMock** | HTTP mocking in tests |
| **Reactor** | Reactive programming |
| **Jackson** | JSON serialization |
| **Log4j2** | Logging framework |
| **JUnit 5** | Testing framework |
| **AssertJ** | Fluent assertions |

---

## Service Breakdown

### Service Overview

| Service | Port | Debug Port | Database | Purpose |
|---------|------|------------|----------|---------|
| **ServiceRegistry** | 8761 | - | - | Eureka Server for service discovery |
| **ConfigServer** | 9296 | - | - | Centralized configuration management |
| **CloudGateway** | 9090 | - | - | API Gateway, routing, authentication |
| **ProductService** | 8081 | 5005 | productDb (3306) | Product catalog management |
| **OrderService** | 8082 | 5006 | orderDb (3307) | Order processing, orchestration |
| **PaymentService** | 8083 | 5007 | paymentDb (3308) | Payment processing |

### Service Dependencies

```
CloudGateway
├── ServiceRegistry (discovery)
├── ConfigServer (configuration)
├── Auth0 (authentication)
├── ProductService (routing)
├── OrderService (routing)
└── PaymentService (routing)

OrderService
├── ServiceRegistry (discovery)
├── ConfigServer (configuration)
├── ProductService (REST call)
├── PaymentService (REST call)
├── Auth0 (OAuth2 client)
└── orderDb (MySQL)

ProductService
├── ServiceRegistry (discovery)
├── ConfigServer (configuration)
├── Auth0 (JWT validation)
└── productDb (MySQL)

PaymentService
├── ServiceRegistry (discovery)
├── ConfigServer (configuration)
├── Auth0 (JWT validation)
└── paymentDb (MySQL)
```

### Service Communication Flow

```
User Request
    ↓
CloudGateway (9090)
    ↓ (JWT validation)
    ├→ ProductService (8081) → productDb
    ├→ OrderService (8082) → orderDb
    │       ├→ ProductService (via RestTemplate)
    │       └→ PaymentService (via Feign)
    └→ PaymentService (8083) → paymentDb
```

---

## Best Practices Implemented

### ✅ Microservices Design Patterns

| Pattern | Status | Implementation |
|---------|--------|----------------|
| **Service Discovery** | ✅ | Netflix Eureka |
| **API Gateway** | ✅ | Spring Cloud Gateway |
| **Circuit Breaker** | ✅ | Resilience4j |
| **Centralized Configuration** | ✅ | Spring Cloud Config |
| **Database per Service** | ✅ | Separate MySQL databases |
| **Load Balancing** | ✅ | Spring Cloud LoadBalancer |
| **Authentication at Gateway** | ✅ | OAuth2/JWT |
| **Service-to-Service Auth** | ✅ | OAuth2 Client Credentials |
| **Externalized Configuration** | ✅ | Environment variables |
| **Health Checks** | ✅ | Actuator + Docker healthchecks |
| **API Documentation** | ✅ | OpenAPI/Swagger |
| **Containerization** | ✅ | Docker + Docker Compose |
| **Orchestration Ready** | ✅ | Kubernetes manifests |
| **Hot Reload (Dev)** | ✅ | DevTools + volume mounts |

### ✅ Security Best Practices

| Practice | Status | Details |
|----------|--------|---------|
| **No Hardcoded Secrets** | ✅ | All credentials in `.env` |
| **Separate Auth Apps** | ✅ | Web App + M2M App |
| **JWT Validation** | ✅ | All services validate tokens |
| **Automatic Token Refresh** | ✅ | Refresh token support |
| **CORS Protection** | ✅ | Specific allowed origins |
| **HTTPS Ready** | ✅ | Configuration supports TLS |
| **Defense in Depth** | ✅ | Multiple security layers |
| **Principle of Least Privilege** | ✅ | Minimal scopes/permissions |
| **Secrets in .gitignore** | ✅ | `.env` never committed |

### ✅ Development Best Practices

| Practice | Status | Details |
|----------|--------|---------|
| **Hot Reload** | ✅ | No rebuild needed in dev |
| **Remote Debugging** | ✅ | Debug ports exposed |
| **Dev Containers** | ✅ | Consistent dev environment |
| **Shared Maven Cache** | ✅ | Faster builds |
| **Profile-Based Config** | ✅ | Dev/prod separation |
| **Automated Testing** | ✅ | Unit + integration tests |
| **API Documentation** | ✅ | Auto-generated from code |
| **Code Generation** | ✅ | Lombok reduces boilerplate |

### ✅ Operational Best Practices

| Practice | Status | Details |
|----------|--------|---------|
| **Health Checks** | ✅ | All services monitored |
| **Graceful Degradation** | ✅ | Circuit breaker fallbacks |
| **Service Isolation** | ✅ | Independent deployments |
| **Horizontal Scaling** | ✅ | Load balancer ready |
| **Logging** | ✅ | Structured logging |
| **Monitoring Endpoints** | ✅ | Actuator |
| **Dependency Management** | ✅ | Maven BOM |
| **Version Control** | ✅ | Git |

---

## Future Enhancements

### ✅ Ready to Enable (Dependencies Already Present)

These features have dependencies already in the codebase but are commented out or disabled:

#### 1. Distributed Tracing ⚡ Quick Win
**Status**: ✅ Dependencies present but commented out
**Location**: `OrderService/pom.xml` lines 95-106
**Action Required**:
1. Uncomment these dependencies:
   ```xml
   <dependency>
       <groupId>io.micrometer</groupId>
       <artifactId>micrometer-observation</artifactId>
   </dependency>
   <dependency>
       <groupId>io.micrometer</groupId>
       <artifactId>micrometer-tracing-bridge-brave</artifactId>
   </dependency>
   <dependency>
       <groupId>io.zipkin.reporter2</groupId>
       <artifactId>zipkin-reporter-brave</artifactId>
   </dependency>
   ```
2. Add configuration:
   ```yaml
   management:
     tracing:
       sampling:
         probability: 1.0
     zipkin:
       tracing:
         endpoint: http://localhost:9411/api/v2/spans
   ```
3. Start Zipkin:
   ```bash
   docker run -d -p 9411:9411 openzipkin/zipkin
   ```

**Tools**: Micrometer Tracing + Zipkin
**Effort**: 30 minutes
**Benefit**: Complete request tracing across all microservices

---

### 🔨 Easy to Add (Simple Configuration)

These features require adding dependencies but have straightforward setup:

#### 2. API Rate Limiting ⚡ Quick Win
**Status**: ❌ Not implemented (Gateway supports it natively)
**Action Required**:
1. Add Redis dependency to CloudGateway:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
   </dependency>
   ```
2. Add rate limiter filter in `application-dev.yml`:
   ```yaml
   spring:
     cloud:
       gateway:
         routes:
           - id: PRODUCT-SERVICE
             filters:
               - name: RequestRateLimiter
                 args:
                   redis-rate-limiter.replenishRate: 10
                   redis-rate-limiter.burstCapacity: 20
   ```
3. Start Redis:
   ```bash
   docker run -d -p 6379:6379 redis:alpine
   ```

**Tools**: Redis + Spring Cloud Gateway Rate Limiter
**Effort**: 1 hour
**Benefit**: Prevent API abuse, ensure fair usage

#### 3. API Versioning ⚡ Quick Win
**Status**: ❌ Not implemented (requires code changes only)
**Action Required**:
1. Add version prefix to controllers:
   ```java
   @RequestMapping("/api/v1/products")
   public class ProductController { }
   ```
2. Update gateway routes:
   ```yaml
   - Path=/api/v1/product/**
   ```

**Effort**: 2 hours
**Benefit**: Backward compatibility, gradual migrations

---

### 🛠️ Moderate Effort (Requires Infrastructure)

#### 4. Database Migration
**Status**: ❌ Not implemented (currently using JPA auto-DDL)
**Action Required**:
1. Add Flyway dependency (recommended):
   ```xml
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-core</artifactId>
   </dependency>
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-mysql</artifactId>
   </dependency>
   ```
2. Disable JPA auto-DDL:
   ```yaml
   spring:
     jpa:
       hibernate:
         ddl-auto: validate  # Change from 'update'
   ```
3. Create migration scripts:
   ```
   src/main/resources/db/migration/
   └── V1__initial_schema.sql
   └── V2__add_product_category.sql
   ```

**Tools**: Flyway or Liquibase
**Effort**: 1 day (includes creating initial migrations)
**Benefit**: Version-controlled schema changes, safe deployments

#### 5. Caching Layer
**Status**: ❌ Not implemented
**Action Required**:
1. Add Redis + Spring Cache dependencies:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-redis</artifactId>
   </dependency>
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-cache</artifactId>
   </dependency>
   ```
2. Enable caching:
   ```java
   @EnableCaching
   @SpringBootApplication
   public class ProductServiceApplication { }
   ```
3. Add cache annotations:
   ```java
   @Cacheable("products")
   public Product getProductById(Long id) { }
   ```

**Tools**: Redis + Spring Cache
**Effort**: 1 day
**Benefit**: Reduced database load, 10x faster reads

#### 6. Metrics & Monitoring
**Status**: ⚠️ Partially ready (Actuator endpoints exist)
**Action Required**:
1. Add Micrometer Prometheus registry:
   ```xml
   <dependency>
       <groupId>io.micrometer</groupId>
       <artifactId>micrometer-registry-prometheus</artifactId>
   </dependency>
   ```
2. Expose Prometheus endpoint:
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,info,prometheus
   ```
3. Start Prometheus + Grafana:
   ```bash
   docker-compose -f monitoring-compose.yml up -d
   ```

**Tools**: Prometheus + Grafana + Micrometer
**Effort**: 2 days (includes dashboard creation)
**Benefit**: Real-time metrics, alerting, performance monitoring

---

### 🚀 Advanced Features (Significant Effort)

These features require architectural changes and significant development effort:

#### 7. Message Queue (Async Communication)
**Status**: ❌ Not implemented
**Complexity**: High
**Effort**: 1-2 weeks

**Action Required**:
1. Add RabbitMQ or Kafka dependency
2. Define message schemas/events
3. Implement event publishers
4. Implement event consumers
5. Handle event failures and retries
6. Update service-to-service communication

**Tools**: RabbitMQ (recommended for getting started) or Apache Kafka (for high throughput)
**Benefit**: Event-driven architecture, loose coupling, better scalability
**Use Case**:
- Order created → Inventory reserved → Payment processed → Email sent
- Decoupled service communication
- Event replay for debugging

**Example Architecture**:
```
OrderService → [OrderCreated Event] → RabbitMQ → PaymentService
                                               → EmailService
                                               → InventoryService
```

#### 8. SAGA Pattern (Distributed Transactions)
**Status**: ❌ Not implemented
**Complexity**: Very High
**Effort**: 2-3 weeks

**Prerequisites**: Message Queue must be implemented first

**Action Required**:
1. Choose SAGA type (Choreography or Orchestration)
2. Define compensation logic for each step
3. Implement saga coordinator (if orchestration)
4. Handle partial failures
5. Implement idempotency
6. Add saga state management

**Benefit**: Distributed transaction management, data consistency
**Use Case**: Order placement across multiple services with rollback capability

**Example Flow**:
```
1. Reserve Inventory (compensate: Release Inventory)
2. Process Payment (compensate: Refund Payment)
3. Create Order (compensate: Cancel Order)
4. Send Confirmation (compensate: Send Cancellation)
```

#### 9. Event Sourcing
**Status**: ❌ Not implemented
**Complexity**: Very High
**Effort**: 3-4 weeks

**Action Required**:
1. Choose event store (EventStoreDB, Axon Framework)
2. Design event schemas
3. Implement event persistence
4. Build read models (CQRS)
5. Implement event replay
6. Handle schema evolution

**Benefit**: Complete audit trail, event replay, time-travel debugging
**Use Case**: Financial transactions, order history, audit requirements

#### 10. Service Mesh
**Status**: ❌ Not implemented (Kubernetes prerequisite)
**Complexity**: High
**Effort**: 1-2 weeks

**Prerequisites**: Kubernetes deployment

**Action Required**:
1. Choose service mesh (Istio recommended, Linkerd for simplicity)
2. Install mesh control plane
3. Inject sidecar proxies
4. Configure traffic policies
5. Set up mutual TLS
6. Configure observability

**Tools**: Istio or Linkerd
**Benefit**: Advanced traffic management, security, observability without code changes
**Features**:
- Automatic mutual TLS
- Circuit breaking (replaces Resilience4j)
- Advanced routing (A/B testing, canary deployments)
- Distributed tracing (automatic)
- Service-to-service authorization

#### 11. CQRS Pattern
**Status**: ❌ Not implemented
**Complexity**: High
**Effort**: 2-3 weeks

**Action Required**:
1. Separate read and write data models
2. Implement command handlers
3. Implement query handlers
4. Set up event propagation (write → read)
5. Handle eventual consistency
6. Optimize read models for queries

**Benefit**: Separate read/write scaling, optimized queries
**Use Case**: High-read, low-write scenarios (product catalog, reporting)

#### 12. GraphQL Gateway
**Status**: ❌ Not implemented
**Complexity**: Moderate
**Effort**: 1-2 weeks

**Action Required**:
1. Add Spring for GraphQL dependency
2. Define GraphQL schemas
3. Implement resolvers
4. Add federation (for microservices)
5. Set up GraphQL gateway

**Tools**: Spring for GraphQL
**Benefit**: Flexible queries, reduced over-fetching, single endpoint
**Use Case**: Mobile apps, complex frontend requirements

---

### 📊 Summary Table

| Feature | Status | Effort | Complexity | Prerequisites | ROI |
|---------|--------|--------|------------|---------------|-----|
| **Distributed Tracing** | ✅ Ready (commented) | 30 min | Low | Zipkin container | ⭐⭐⭐⭐⭐ |
| **API Rate Limiting** | ❌ Not implemented | 1 hour | Low | Redis container | ⭐⭐⭐⭐ |
| **API Versioning** | ❌ Not implemented | 2 hours | Low | None | ⭐⭐⭐⭐ |
| **Database Migration** | ❌ Not implemented | 1 day | Low | None | ⭐⭐⭐⭐⭐ |
| **Caching** | ❌ Not implemented | 1 day | Moderate | Redis | ⭐⭐⭐⭐⭐ |
| **Metrics/Monitoring** | ⚠️ Partial | 2 days | Moderate | Prometheus/Grafana | ⭐⭐⭐⭐⭐ |
| **Message Queue** | ❌ Not implemented | 1-2 weeks | High | RabbitMQ/Kafka | ⭐⭐⭐⭐ |
| **SAGA Pattern** | ❌ Not implemented | 2-3 weeks | Very High | Message Queue | ⭐⭐⭐ |
| **Event Sourcing** | ❌ Not implemented | 3-4 weeks | Very High | Event Store | ⭐⭐⭐ |
| **Service Mesh** | ❌ Not implemented | 1-2 weeks | High | Kubernetes | ⭐⭐⭐⭐ |
| **CQRS** | ❌ Not implemented | 2-3 weeks | High | Separate DB | ⭐⭐⭐ |
| **GraphQL** | ❌ Not implemented | 1-2 weeks | Moderate | None | ⭐⭐⭐ |

---

### 🎯 Recommended Implementation Order

**Phase 1: Quick Wins (Week 1)**
1. ✅ Enable Distributed Tracing (30 min)
2. ✅ Add API Rate Limiting (1 hour)
3. ✅ Implement API Versioning (2 hours)
4. ✅ Add Database Migration (1 day)

**Phase 2: Performance & Monitoring (Week 2-3)**
5. ✅ Implement Caching Layer (1 day)
6. ✅ Set up Metrics & Monitoring (2 days)

**Phase 3: Advanced Patterns (Month 2)**
7. ✅ Implement Message Queue (1-2 weeks)
8. ✅ Add SAGA Pattern (2-3 weeks)

**Phase 4: Enterprise Features (Month 3+)**
9. ✅ Deploy to Kubernetes
10. ✅ Implement Service Mesh
11. ✅ Consider Event Sourcing or CQRS based on requirements

---

## Architecture Diagrams

### System Context Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                                                               │
│                      External Systems                         │
│                                                               │
│  ┌──────────┐              ┌──────────┐                     │
│  │  Auth0   │              │ Frontend │                     │
│  │ (OAuth2) │              │   Apps   │                     │
│  └────┬─────┘              └────┬─────┘                     │
│       │                         │                           │
└───────┼─────────────────────────┼───────────────────────────┘
        │                         │
        │   ┌─────────────────────┼────────────────────────┐
        │   │                     ▼                        │
        │   │          ┌─────────────────────┐            │
        └───┼──────────┤   CloudGateway      │            │
            │          │   (Port 9090)       │            │
            │          └──────────┬──────────┘            │
            │                     │                        │
            │       ┌─────────────┼─────────────┐         │
            │       │             │             │         │
            │       ▼             ▼             ▼         │
            │  ┌─────────┐  ┌─────────┐  ┌─────────┐    │
            │  │ Product │  │  Order  │  │ Payment │    │
            │  │ Service │  │ Service │  │ Service │    │
            │  │  8081   │  │  8082   │  │  8083   │    │
            │  └────┬────┘  └────┬────┘  └────┬────┘    │
            │       │            │            │         │
            │       ▼            ▼            ▼         │
            │  ┌─────────┐  ┌─────────┐  ┌─────────┐   │
            │  │productDb│  │ orderDb │  │paymentDb│   │
            │  │  MySQL  │  │  MySQL  │  │  MySQL  │   │
            │  └─────────┘  └─────────┘  └─────────┘   │
            │                                            │
            │  Infrastructure Services:                 │
            │  ┌────────────────┐  ┌──────────────┐    │
            │  │ Eureka Server  │  │ Config Server│    │
            │  │    (8761)      │  │    (9296)    │    │
            │  └────────────────┘  └──────────────┘    │
            │                                            │
            └────────────────────────────────────────────┘
                    Spring Boot Microservices
```

### OAuth2 Authentication Flow

```
┌──────────┐
│ Frontend │
└─────┬────┘
      │ (1) GET /authenticate/login
      ▼
┌─────────────────┐
│  CloudGateway   │
│  Regular Web    │
│  Application    │
└────┬───────────┘
     │ (2) Redirect to Auth0 login
     ▼
┌─────────────────┐
│     Auth0       │
│  Login Page     │
└────┬───────────┘
     │ (3) User authenticates
     │ (4) Redirect with auth code
     ▼
┌─────────────────┐
│  CloudGateway   │
└────┬───────────┘
     │ (5) Exchange code for tokens
     │ (6) Return tokens to frontend
     ▼
┌──────────┐
│ Frontend │ ← Access Token, Refresh Token
└──────────┘
     │
     │ (7) API request with Bearer token
     ▼
┌─────────────────┐
│  CloudGateway   │
└────┬───────────┘
     │ (8) Validate JWT
     │ (9) Route to service
     ▼
┌─────────────────┐
│ Business Service│
│ (validates JWT) │
└─────────────────┘
```

### Service-to-Service Authentication

```
┌─────────────────┐
│  OrderService   │
│  (M2M Client)   │
└────┬───────────┘
     │ (1) Need to call ProductService
     │
     ▼
┌─────────────────────────────┐
│ RestTemplate Interceptor    │
└────┬───────────────────────┘
     │ (2) Check token cache
     │ (3) If expired, request new token
     ▼
┌─────────────────┐
│     Auth0       │
│ (M2M App)       │
└────┬───────────┘
     │ (4) Client Credentials grant
     │ (5) Return access token
     ▼
┌─────────────────────────────┐
│ RestTemplate Interceptor    │
└────┬───────────────────────┘
     │ (6) Add Authorization: Bearer <token>
     ▼
┌─────────────────┐
│ ProductService  │
│ (validates JWT) │
└─────────────────┘
```

---

## Conclusion

This Spring Boot microservices application demonstrates a comprehensive implementation of modern cloud-native patterns and best practices. It provides:

✅ **Scalability**: Horizontal scaling with load balancing
✅ **Resilience**: Circuit breakers and fault tolerance
✅ **Security**: OAuth2/JWT with Auth0
✅ **Observability**: Health checks, logging, monitoring
✅ **Developer Experience**: Hot reload, debugging, documentation
✅ **Production Ready**: Containerization, Kubernetes manifests
✅ **Maintainability**: Clean architecture, separation of concerns

The architecture is designed to be extended with additional patterns like event-driven communication, CQRS, and distributed tracing as the application grows.

---

**Last Updated**: October 5, 2025
**Version**: 1.0.0
**Author**: Microservices Team
