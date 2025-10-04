
![img_3.png](img_3.png)

# Spring Boot Microservices E-Commerce Application

A cloud-native e-commerce application built with Spring Boot microservices architecture, featuring service discovery, centralized configuration, API gateway, and comprehensive deployment options using Docker and Kubernetes.

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Microservices](#microservices)
- [Key Features](#key-features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Deployment](#deployment)
- [Observability](#observability)
- [Notes](#notes)

## Architecture Overview

This project implements a microservices architecture with the following components:

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Cloud Gateway  │ (Port 9090) - API Gateway + Circuit Breaker
└────────┬────────┘
         │
    ┌────┴────┬────────────┬──────────────┐
    ▼         ▼            ▼              ▼
┌─────────┐ ┌──────────┐ ┌──────────┐ ┌────────────────┐
│ Product │ │  Order   │ │ Payment  │ │ ServiceRegistry│
│ Service │ │ Service  │ │ Service  │ │  (Eureka)      │
└─────────┘ └──────────┘ └──────────┘ └────────────────┘
                                       ┌────────────────┐
                                       │ Config Server  │
                                       └────────────────┘
```

## Technology Stack

- **Framework:** Spring Boot 3.1.5 - 3.2.0
- **Java Version:** 17
- **Spring Cloud:** 2022.0.4 / 2023.0.0-RC1
- **Database:**
  - MySQL (Production)
  - H2 (Testing)
- **Security:**
  - Spring Security
  - OAuth2 with Okta
- **Service Discovery:** Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Resilience:** Circuit Breaker Pattern
- **Inter-service Communication:**
  - OpenFeign
  - RestTemplate with Load Balancing
- **Observability:**
  - Micrometer
  - Zipkin (Distributed Tracing)
  - Spring Boot Actuator
- **Build Tools:**
  - Maven
  - Jib (Container Image Build)
- **Containerization:** Docker & Kubernetes
- **Other Libraries:**
  - Lombok
  - WireMock (Testing)

## Microservices

### 1. Service Registry (Port 8761)
- **Purpose:** Service discovery using Netflix Eureka
- **Technology:** Spring Cloud Netflix Eureka Server
- **Functionality:** Maintains registry of all microservice instances

### 2. Config Server (Port 9296)
- **Purpose:** Centralized configuration management
- **Technology:** Spring Cloud Config Server
- **Functionality:** Provides externalized configuration for all services

### 3. Cloud Gateway (Port 9090)
- **Purpose:** API Gateway and routing
- **Technology:** Spring Cloud Gateway
- **Functionality:**
  - Single entry point for all client requests
  - Circuit breaker implementation for all routes
  - OAuth2 authentication
  - Request routing to appropriate microservices
  - Fallback mechanisms

### 4. Product Service
- **Purpose:** Product catalog management
- **Database:** MySQL (productDb)
- **Functionality:**
  - Create products
  - Retrieve product information
  - Update product inventory
  - Product stock management

### 5. Order Service
- **Purpose:** Order processing and management
- **Database:** MySQL (orderDb)
- **Functionality:**
  - Place orders
  - Retrieve order details
  - Communicate with Product and Payment services
  - OAuth2 client credentials flow

### 6. Payment Service
- **Purpose:** Payment transaction handling
- **Database:** MySQL (paymentDb)
- **Functionality:**
  - Process payments
  - Store transaction details
  - Support multiple payment modes (CASH, UPI, etc.)

## Key Features

### Security
- OAuth2 authentication with Okta integration
- Bearer token authorization for all API endpoints
- RestTemplate interceptor for OAuth2 client credentials flow
- Spring Security configuration across all services

### Resilience & Fault Tolerance
- Circuit breaker pattern implemented for all gateway routes
- Fallback controllers for graceful service failure handling
- Health checks configured in Docker Compose
- Load-balanced inter-service communication

### Cloud-Native
- Service discovery with Eureka
- Centralized configuration management
- Containerized deployment with Docker
- Kubernetes-ready with complete manifests
- Distributed tracing with Zipkin

### Observability
- Spring Boot Actuator endpoints
- Micrometer for metrics collection
- Zipkin integration for distributed tracing
- Health and info endpoints

## Project Structure

```
SpringBoot/
├── ServiceRegistry/              # Eureka Server
│   ├── src/
│   ├── Dockerfile               # Production Dockerfile
│   ├── Dockerfile.dev           # Development Dockerfile (hot reload)
│   └── pom.xml
├── ConfigServer/                 # Configuration Server
│   ├── src/
│   ├── Dockerfile               # Production Dockerfile
│   ├── Dockerfile.dev           # Development Dockerfile (hot reload)
│   └── pom.xml
├── CloudGateway/                 # API Gateway
│   ├── src/
│   ├── Dockerfile               # Production Dockerfile
│   ├── Dockerfile.dev           # Development Dockerfile (hot reload)
│   └── pom.xml
├── ProductService/               # Product Microservice
│   ├── src/
│   ├── Dockerfile               # Production Dockerfile
│   ├── Dockerfile.dev           # Development Dockerfile (hot reload + debug)
│   └── pom.xml
├── OrderService/                 # Order Microservice
│   ├── src/
│   ├── Dockerfile               # Production Dockerfile
│   ├── Dockerfile.dev           # Development Dockerfile (hot reload + debug)
│   └── pom.xml
├── PaymentService/               # Payment Microservice
│   ├── src/
│   ├── Dockerfile               # Production Dockerfile
│   ├── Dockerfile.dev           # Development Dockerfile (hot reload + debug)
│   └── pom.xml
├── k8s/                          # Kubernetes Manifests
│   ├── service-registry-statefulset.yml
│   ├── config-server-deployment.yml
│   ├── cloud-gateway-deployment.yml
│   ├── product-service-deployment.yml
│   ├── order-service-deployment.yml
│   ├── payment-service-deployment.yml
│   ├── mysql-deployment.yml
│   └── config-maps.yml
├── docker-compose.yml            # Production stack
├── docker-compose.dev.yml        # Development stack (with hot reload)
└── README.md
```

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Docker** and **Docker Compose**
- **Kubernetes** cluster (for K8s deployment)
- **Okta Account** (for OAuth2 authentication)
- **MySQL 8.0+** (for production deployment)

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd SpringBoot
```

### 2. Configure OAuth2 (Okta)

Update the Okta configuration in `CloudGateway/src/main/resources/application-dev.yml`:

```yaml
okta:
  oauth2:
    issuer: https://your-okta-domain/oauth2/default
    audience: api://default
    client-id: your-client-id
    client-secret: your-client-secret
    scopes: openid, profile, email, offline_access
```

### 3. Build and Run with Docker Compose

All services now have Dockerfiles and can be built with docker-compose:

```bash
# Build JAR files
mvn clean package -DskipTests

# Build Docker images and start services
docker-compose up --build -d
```

**Note:**
- Infrastructure services (ServiceRegistry, ConfigServer, CloudGateway) use pre-built images from Docker Hub
- Business services (Product, Order, Payment) are built from local Dockerfiles

### 4. Run Complete Stack with Docker Compose

```bash
# Start all services (infrastructure + databases + business services)
docker-compose up -d

# View logs for all services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f productservice
```

This will start:
- **Infrastructure:**
  - Service Registry (http://localhost:8761)
  - Config Server (http://localhost:9296)
  - Cloud Gateway (http://localhost:9090)
- **Databases:**
  - Product DB (MySQL on port 3306)
  - Order DB (MySQL on port 3307)
  - Payment DB (MySQL on port 3308)
- **Business Services:**
  - Product Service (port 8081)
  - Order Service (port 8082)
  - Payment Service (port 8083)

### 5. Verify Services

```bash
# Check Eureka Dashboard - You should see all services registered
open http://localhost:8761

# Check service health
curl http://localhost:8761/actuator/health  # Service Registry
curl http://localhost:9296/actuator/health  # Config Server
curl http://localhost:9090/actuator/health  # Cloud Gateway

# Check if all containers are running
docker-compose ps

# Check MySQL databases
docker exec -it productDb mysql -uroot -proot -e "SHOW DATABASES;"
docker exec -it orderDb mysql -uroot -proot -e "SHOW DATABASES;"
docker exec -it paymentDb mysql -uroot -proot -e "SHOW DATABASES;"
```

### 6. Managing the Stack

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This deletes all database data)
docker-compose down -v

# Restart a specific service
docker-compose restart productservice

# Rebuild and restart a specific service
cd ProductService && mvn clean compile jib:dockerBuild && cd ..
docker-compose up -d --force-recreate productservice

# View resource usage
docker-compose stats
```

## API Documentation

This section provides an overview of the API endpoints for creating products, fetching product details by ID, placing orders, and retrieving order details. Each section includes the necessary `curl` commands to interact with the API.

## Base URL
The base URL for all API requests is:
```
http://localhost:9090
```

## Authentication
All API requests require an Authorization header with a Bearer token:
```
--header 'Authorization: Bearer xxxx'
```

Replace `xxxx` with your actual token.

## Endpoints

### 1. Create a Product
Creates a new product in the system.

**Endpoint:**
```
POST /product
```

**Request:**
```bash
curl --location 'http://localhost:9090/product' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer xxxx' \
--data '{
    "name": "iPhone 15",
    "price": 2000,
    "quantity": 2
}'
```

**Request Body:**
- `name` (string): The name of the product.
- `price` (number): The price of the product.
- `quantity` (number): The quantity of the product in stock.

### 2. Fetch Product by ID
Fetches details of a product by its ID.

**Endpoint:**
```
GET /product/{id}
```

**Request:**
```bash
curl --location --request GET 'http://localhost:9090/product/103' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer xxxx'
```

**Path Parameter:**
- `id` (number): The ID of the product to fetch.

**Note:** The request body is not required for GET requests.

### 3. Place an Order
Places an order for a product.

**Endpoint:**
```
POST /order/placeOrder
```

**Request:**
```bash
curl --location 'http://localhost:9090/order/placeOrder' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer xxxx' \
--data '{
    "productId": 103,
    "totalAmount": 10000,
    "quantity": 1,
    "paymentMode": "CASH"
}'
```

**Request Body:**
- `productId` (number): The ID of the product to order.
- `totalAmount` (number): The total amount for the order.
- `quantity` (number): The quantity of the product to order.
- `paymentMode` (string): The payment mode for the order (e.g., "CASH").

### 4. Get Order Details
Fetches details of an order by its ID.

**Endpoint:**
```
GET /order/{id}
```

**Request:**
```bash
curl --location 'http://localhost:9090/order/2' \
--header 'Authorization: Bearer xxxx'
```

**Path Parameter:**
- `id` (number): The ID of the order to fetch.

**Note:** The request body is not required for GET requests.

## Local Development

The project includes a dedicated development environment using `docker-compose.dev.yml` that makes local development much easier.

### Development Modes

#### Mode 1: Hybrid Development (Recommended)

Run infrastructure in Docker, code business services locally for hot reload:

**Quick Start:**
```bash
# Start infrastructure + databases (with hot reload support)
docker-compose -f docker-compose.dev.yml up -d --build serviceregistry configserver cloudgateway productDb orderDb paymentDb
```

**Note:** Now uses `Dockerfile.dev` with volume mounts for source code hot reload!

This starts:
- ✓ Service Registry (Eureka)
- ✓ Config Server
- ✓ Cloud Gateway
- ✓ All 3 MySQL Databases

**Then run the service you're developing:**
```bash
# Terminal 1 - Product Service (with hot reload)
cd ProductService
mvn spring-boot:run

# Terminal 2 - Order Service (with hot reload)
cd OrderService
mvn spring-boot:run

# Terminal 3 - Payment Service (with hot reload)
cd PaymentService
mvn spring-boot:run
```

**Benefits:**
- 🚀 Instant code changes (hot reload with Spring Boot DevTools)
- 🐛 Easy debugging in IDE
- 💻 Only run what you're editing
- 🔧 Infrastructure runs in Docker (no manual setup)

#### Mode 2: Full Stack in Docker with Hot Reload

Run everything in Docker with automatic code reload:

```bash
# Start all services with hot reload
docker-compose -f docker-compose.dev.yml up --build -d
```

This starts ALL 9 services in Docker:
- ✓ Service Registry, Config Server, Cloud Gateway
- ✓ Product, Order, Payment Services
- ✓ All 3 MySQL Databases

**NEW Development Features:**
- 🔥 **Hot Reload**: Code changes automatically reload in containers!
- 🐛 **Remote Debugging**: Debug ports exposed (5005-5007)
- 💾 **Volume Mounts**: Source code mounted for instant updates
- ⚡ **Maven Cache**: Shared Maven repo for faster builds

**Benefits:**
- 📦 Test complete system integration
- 🚀 Code changes reload automatically (Spring Boot DevTools)
- 🐛 Remote debugging from IDE
- 💻 Edit code locally, runs in Docker
- 🔄 Production-like environment

**When to use:**
- Full stack development with hot reload
- Integration testing
- Testing multi-service interactions
- Debugging across services

### Development Commands

```bash
# Start infrastructure only (hybrid mode)
docker-compose -f docker-compose.dev.yml up -d --build serviceregistry configserver cloudgateway productDb orderDb paymentDb

# Start all services (full stack with hot reload)
docker-compose -f docker-compose.dev.yml up -d --build

# View logs
docker-compose -f docker-compose.dev.yml logs -f

# View specific service logs
docker-compose -f docker-compose.dev.yml logs -f productservice

# Stop development environment
docker-compose -f docker-compose.dev.yml down

# Rebuild a specific service after dependency changes
docker-compose -f docker-compose.dev.yml up -d --build productservice

# Check service status
docker-compose -f docker-compose.dev.yml ps
```

### Manual Development (Without Docker)

If you prefer running everything manually:

```bash
# Build all services
mvn clean package -DskipTests
```

**Run services in separate terminals (in this order):**

1. **Service Registry** (Terminal 1):
```bash
cd ServiceRegistry && mvn spring-boot:run
```

2. **Config Server** (Terminal 2):
```bash
cd ConfigServer && mvn spring-boot:run
```

3. **Cloud Gateway** (Terminal 3):
```bash
cd CloudGateway && mvn spring-boot:run
```

4. **Product Service** (Terminal 4):
```bash
cd ProductService && mvn spring-boot:run
```

5. **Order Service** (Terminal 5):
```bash
cd OrderService && mvn spring-boot:run
```

6. **Payment Service** (Terminal 6):
```bash
cd PaymentService && mvn spring-boot:run
```

**Note:** You'll also need to run MySQL locally on ports 3306, 3307, 3308.

### Development Dockerfiles Explained

The project has **two sets of Dockerfiles**:

#### `Dockerfile` (Production)
```dockerfile
FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} productservice.jar
ENTRYPOINT ["java", "-jar", "/productservice.jar"]
```
- ✅ Small image size (Alpine)
- ✅ Fast startup
- ❌ No hot reload
- ❌ Requires rebuild on code changes

#### `Dockerfile.dev` (Development)
```dockerfile
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
ENTRYPOINT ["mvn", "spring-boot:run"]
```
- ✅ **Hot reload** with Spring Boot DevTools
- ✅ **Volume mounts** for instant code changes
- ✅ **Remote debugging** enabled
- ✅ No rebuild needed for code changes
- ⚠️ Larger image, slower initial build

### Development Tips

**Hot Reload with Docker:**
```bash
# Start services with hot reload
docker-compose -f docker-compose.dev.yml up -d

# Edit code in ProductService/src/...
# Changes reload automatically! ✨
```

**Remote Debugging:**
- **ProductService**: localhost:5005
- **OrderService**: localhost:5006
- **PaymentService**: localhost:5007

**IntelliJ IDEA Setup:**
1. Run → Edit Configurations → Add Remote JVM Debug
2. Host: localhost, Port: 5005 (or 5006, 5007)
3. Set breakpoints and start debugging!

**VS Code Setup:**
1. Install "Java Extension Pack"
2. Add debug configuration for remote attach
3. Connect to localhost:5005

**Database Access:**
```bash
# Connect to development databases
mysql -h 127.0.0.1 -P 3306 -u root -proot productDb
mysql -h 127.0.0.1 -P 3307 -u root -proot orderDb
mysql -h 127.0.0.1 -P 3308 -u root -proot paymentDb
```

**Important Notes:**
- Code changes in `/src` automatically reload
- Dependency changes (pom.xml) require rebuild: `docker-compose -f docker-compose.dev.yml up -d --build`
- Maven dependencies cached in shared volume for faster builds

## Deployment

### Docker Deployment

The project includes a comprehensive `docker-compose.yml` that orchestrates all services:
- **9 containers total:** 3 infrastructure services, 3 MySQL databases, 3 business services
- **Automatic service dependencies:** Services start in the correct order
- **Health checks:** Ensures databases and services are ready before dependent services start
- **Isolated network:** All services communicate on a dedicated Docker bridge network
- **Persistent volumes:** Database data is preserved across container restarts

#### Quick Start (Automated Script)

```bash
# Use the automated build and start script
./build-and-start.sh
```

This script will:
1. Build JAR files for all services with Maven
2. Build Docker images using docker-compose build
3. Start all 9 containers with docker-compose
4. Display service status and useful URLs

#### Manual Start

```bash
# Step 1: Build JAR files
mvn clean package -DskipTests

# Step 2: Build Docker images
docker-compose build

# Step 3: Start entire stack
docker-compose up -d

# Verify all containers are running
docker-compose ps

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

#### Alternative: Build and Start in One Command

```bash
# Build JAR files first
mvn clean package -DskipTests

# Build images and start services
docker-compose up --build -d
```

### Kubernetes Deployment

#### Prerequisites
- Kubernetes cluster (Minikube, GKE, EKS, AKS, etc.)
- kubectl configured

#### Deploy to Kubernetes

```bash
# Apply all manifests
kubectl apply -f k8s/

# Or apply individually
kubectl apply -f k8s/config-maps.yml
kubectl apply -f k8s/mysql-deployment.yml
kubectl apply -f k8s/service-registry-statefulset.yml
kubectl apply -f k8s/config-server-deployment.yml
kubectl apply -f k8s/cloud-gateway-deployment.yml
kubectl apply -f k8s/product-service-deployment.yml
kubectl apply -f k8s/order-service-deployment.yml
kubectl apply -f k8s/payment-service-deployment.yml

# Check deployment status
kubectl get pods
kubectl get services

# Access the application
kubectl port-forward service/cloud-gateway 9090:9090
```

#### Scaling Services

```bash
# Scale product service
kubectl scale deployment product-service --replicas=3

# Scale order service
kubectl scale deployment order-service --replicas=3
```

## Observability

### Health Checks

All services expose health check endpoints via Spring Boot Actuator:

```bash
# Service Registry
curl http://localhost:8761/actuator/health

# Config Server
curl http://localhost:9296/actuator/health

# Cloud Gateway
curl http://localhost:9090/actuator/health
```

### Metrics

Access Actuator metrics:

```bash
curl http://localhost:9090/actuator/metrics
curl http://localhost:9090/actuator/metrics/jvm.memory.used
```

### Distributed Tracing

The application is configured with Zipkin for distributed tracing. When enabled:

- **Zipkin UI:** http://localhost:9411
- Traces are automatically collected for inter-service communication

## Notes

### Current Configuration

- **Docker Compose:** Comprehensive setup with all 9 services (3 infrastructure + 3 databases + 3 business services)
- **Database:** Three separate MySQL 8.0 instances with dedicated volumes for data persistence
- **Networking:** All services communicate via a dedicated Docker bridge network
- **Tracing:** Some tracing dependencies are commented out in `OrderService/pom.xml`

### Known Issues & Recommendations

1. **Version Inconsistency:**
   - ProductService uses Spring Boot 3.1.5
   - OrderService uses Spring Boot 3.2.0
   - Recommend standardizing to a single version

2. **Security Concerns:**
   - Okta credentials in `application-dev.yml` should be externalized to environment variables
   - Use Kubernetes secrets for sensitive data in production

3. **Missing Configuration:**
   - OrderService missing main `application.yaml` (only has `application-dev.yaml`)

4. **Database Setup:**
   - MySQL services are now fully configured in `docker-compose.yml`
   - Each service has its own dedicated database instance
   - Database data persists in Docker volumes

5. **Service Communication:**
   - OrderService uses both RestTemplate and OpenFeign for inter-service communication
   - Consider standardizing on one approach

### Environment Variables

Key environment variables used:

- `EUREKA_SERVER_ADDRESS`: Eureka server URL
- `CONFIG_SERVER_URL`: Config server URL
- `SPRING_PROFILES_ACTIVE`: Active Spring profile (dev, prod, etc.)

### Docker Images

Pre-built images are available on Docker Hub:
- `ebelemgnegre/serviceregistry:0.0.2`
- `ebelemgnegre/configserver:0.0.2`
- `ebelemgnegre/cloudgateway:0.0.2`

## Quick Reference

### Service Ports

| Service | Port | URL |
|---------|------|-----|
| Service Registry (Eureka) | 8761 | http://localhost:8761 |
| Config Server | 9296 | http://localhost:9296 |
| Cloud Gateway | 9090 | http://localhost:9090 |
| Product Service | 8081 | http://localhost:8081 |
| Order Service | 8082 | http://localhost:8082 |
| Payment Service | 8083 | http://localhost:8083 |
| Product DB (MySQL) | 3306 | localhost:3306 |
| Order DB (MySQL) | 3307 | localhost:3307 |
| Payment DB (MySQL) | 3308 | localhost:3308 |

### Complete Setup Commands

**Option 1: Automated (Recommended)**
```bash
# 1. Clone and navigate
git clone <repository-url>
cd SpringBoot

# 2. Configure Okta OAuth2 (edit CloudGateway/src/main/resources/application-dev.yml)

# 3. Build and start everything with one command
./build-and-start.sh

# 4. Verify
open http://localhost:8761  # Check Eureka - all services should be registered

# 5. Test API (see API Documentation section for detailed examples)
curl http://localhost:9090/product/1 \
  -H 'Authorization: Bearer <your-token>'
```

**Option 2: Manual**
```bash
# 1. Clone and navigate
git clone <repository-url>
cd SpringBoot

# 2. Configure Okta OAuth2 (edit CloudGateway/src/main/resources/application-dev.yml)

# 3. Build JAR files and Docker images
mvn clean package -DskipTests
docker-compose build

# 4. Start all services
docker-compose up -d

# 5. Verify
docker-compose ps
open http://localhost:8761  # Check Eureka - all services should be registered

# 6. Test API (see API Documentation section for detailed examples)
curl http://localhost:9090/product/1 \
  -H 'Authorization: Bearer <your-token>'
```

### Troubleshooting

**Services not starting:**
```bash
# Check logs
docker-compose logs -f <service-name>

# Restart a service
docker-compose restart <service-name>
```

**Database connection issues:**
```bash
# Verify MySQL containers are healthy
docker-compose ps

# Check database logs
docker-compose logs productDb
docker-compose logs orderDb
docker-compose logs paymentDb
```

**Image not found errors:**
```bash
# Rebuild JAR and Docker image for specific service
cd <ServiceName>
mvn clean package -DskipTests
cd ..
docker-compose build <servicename>
docker-compose up -d --force-recreate <servicename>
```

**Port conflicts:**
```bash
# Check what's using the port
lsof -i :<port-number>

# Kill the process or change the port in docker-compose.yml
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

[Add your license information here]

---

For any questions or issues, please contact the development team.
