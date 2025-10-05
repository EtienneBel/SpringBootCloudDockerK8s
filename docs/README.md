# Documentation Index

Comprehensive guides for architecture patterns, tools, and technologies used in this Spring Boot Microservices project.

## 📂 Documentation Categories

### 🏗️ Architecture Patterns (`/patterns`)

| Pattern | Description | File |
|---------|-------------|------|
| **Service Discovery** | Netflix Eureka for dynamic service registration | [service-discovery.md](patterns/service-discovery.md) |
| **API Gateway** | Spring Cloud Gateway for routing and security | [api-gateway.md](patterns/api-gateway.md) |
| **Circuit Breaker** | Resilience4j for fault tolerance | [circuit-breaker.md](patterns/circuit-breaker.md) |
| **Externalized Configuration** | Spring Cloud Config Server | [externalized-configuration.md](patterns/externalized-configuration.md) |
| **Database per Service** | Independent databases for each microservice | [database-per-service.md](patterns/database-per-service.md) |
| **Load Balancing** | Client-side load balancing | [load-balancing.md](patterns/load-balancing.md) |
| **Service-to-Service Communication** | RestTemplate & OpenFeign with OAuth2 | [service-communication.md](patterns/service-communication.md) |

### 🔐 Security (`/security`)

| Topic | Description | File |
|-------|-------------|------|
| **OAuth2 Authentication** | Auth0 integration with Spring Security | [oauth2-authentication.md](security/oauth2-authentication.md) |
| **JWT Tokens** | JSON Web Token validation and usage | [jwt-tokens.md](security/jwt-tokens.md) |
| **CORS Configuration** | Cross-Origin Resource Sharing setup | [cors-configuration.md](security/cors-configuration.md) |
| **Service-to-Service Auth** | OAuth2 Client Credentials flow | [service-to-service-auth.md](security/service-to-service-auth.md) |

### 🛠️ Tools & Technologies (`/tools`)

| Tool | Description | File |
|------|-------------|------|
| **Spring Boot** | Core framework overview | [spring-boot.md](tools/spring-boot.md) |
| **Spring Cloud** | Cloud-native patterns and tools | [spring-cloud.md](tools/spring-cloud.md) |
| **Docker** | Containerization guide | [docker.md](tools/docker.md) |
| **Kubernetes** | Container orchestration | [kubernetes.md](tools/kubernetes.md) |
| **Auth0** | OAuth2/OIDC provider setup | [auth0.md](tools/auth0.md) |
| **MySQL** | Database configuration | [mysql.md](tools/mysql.md) |
| **OpenAPI/Swagger** | API documentation | [openapi.md](tools/openapi.md) |
| **Lombok** | Code generation | [lombok.md](tools/lombok.md) |

### 🚀 Deployment (`/deployment`)

| Topic | Description | File |
|-------|-------------|------|
| **Docker Compose** | Local development and deployment | [docker-compose.md](deployment/docker-compose.md) |
| **Kubernetes Deployment** | Production deployment on K8s | [kubernetes-deployment.md](deployment/kubernetes-deployment.md) |
| **Environment Variables** | Configuration management | [environment-variables.md](deployment/environment-variables.md) |
| **CI/CD Pipeline** | Continuous integration and deployment | [cicd-pipeline.md](deployment/cicd-pipeline.md) |

### 💻 Development (`/development`)

| Topic | Description | File |
|-------|-------------|------|
| **Hot Reload** | Development with auto-restart | [hot-reload.md](development/hot-reload.md) |
| **Testing** | Unit, integration, and E2E testing | [testing.md](development/testing.md) |
| **Debugging** | Remote debugging setup | [debugging.md](development/debugging.md) |
| **Dev Containers** | Containerized development environment | [dev-containers.md](development/dev-containers.md) |
| **Logging** | Logging best practices | [logging.md](development/logging.md) |

## 🎯 Quick Start Guides

### For Developers
1. Read [Spring Boot Overview](tools/spring-boot.md)
2. Understand [Service Discovery](patterns/service-discovery.md)
3. Learn [Hot Reload Development](development/hot-reload.md)
4. Set up [Debugging](development/debugging.md)

### For DevOps Engineers
1. Review [Docker Compose Guide](deployment/docker-compose.md)
2. Read [Kubernetes Deployment](deployment/kubernetes-deployment.md)
3. Understand [Environment Variables](deployment/environment-variables.md)
4. Set up [CI/CD Pipeline](deployment/cicd-pipeline.md)

### For Security Engineers
1. Study [OAuth2 Authentication](security/oauth2-authentication.md)
2. Understand [JWT Tokens](security/jwt-tokens.md)
3. Review [Service-to-Service Auth](security/service-to-service-auth.md)
4. Configure [CORS](security/cors-configuration.md)

### For Architects
1. Review all [Architecture Patterns](patterns/)
2. Understand [Service Communication](patterns/service-communication.md)
3. Study [Database per Service](patterns/database-per-service.md)
4. Plan future enhancements (see [ARCHITECTURE.md](../ARCHITECTURE.md#future-enhancements))

## 📚 Main Documentation

- **[Project README](../README.md)** - Quick start and overview
- **[Architecture](../ARCHITECTURE.md)** - Complete technical documentation
- **[Frontend Integration](../FRONTEND_INTEGRATION.md)** - React/Vue/Angular guide
- **[Documentation Standards](DOCUMENTATION_SUMMARY.md)** - For contributors

---

**Need help?** Check the main [README](../README.md) or [DOCUMENTATION_SUMMARY](DOCUMENTATION_SUMMARY.md) for details.
