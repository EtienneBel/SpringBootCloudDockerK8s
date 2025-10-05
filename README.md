
![img_3.png](img_3.png)

# Spring Boot Microservices E-Commerce Application

A cloud-native e-commerce application built with Spring Boot microservices architecture, featuring service discovery, centralized configuration, API gateway, and comprehensive deployment options using Docker and Kubernetes.

## 📚 Documentation

- **[Architecture Documentation](ARCHITECTURE.md)** - Complete technical documentation of patterns, features, and tools
- **[Frontend Integration Guide](FRONTEND_INTEGRATION.md)** - Guide for integrating React/Vue/Angular frontends

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
  - OAuth2 with Auth0
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
- **API Documentation:**
  - Springdoc OpenAPI 3
  - Swagger UI (Interactive API Testing)
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
  - **API Documentation Aggregation:** Uses `springdoc-openapi-starter-webflux-ui` to aggregate OpenAPI docs from all backend microservices into a single Swagger UI interface
- **Centralized Swagger UI:** http://localhost:9090/swagger-ui.html

**Note:** OpenAPI is not required for the gateway's routing functionality, but is useful for providing centralized API documentation and testing interface for all services.

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
- **OAuth2 Authentication:** Auth0 integration for industry-standard authentication
- **JWT Token Validation:** All API requests validated through Cloud Gateway
- **Bearer Token Authorization:** Secure API endpoints with OAuth2 tokens
- **Client Credentials Flow:** Automated service-to-service authentication
- **RestTemplate Interceptor:** Automatic token injection for inter-service calls
- **Spring Security:** Comprehensive security configuration across all services

For detailed Auth0 setup instructions, see [Configure OAuth2 (Auth0)](#2-configure-oauth2-auth0)

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

### API Documentation
- Springdoc OpenAPI 3 integration across all services
- Centralized Swagger UI through Cloud Gateway
- Interactive API testing and exploration
- Auto-generated API specifications from code
- OAuth2 authentication support in Swagger UI

## Project Structure

```
SpringBoot/
├── ServiceRegistry/              # Eureka Server
│   ├── .devcontainer/           # VS Code/Cursor dev container config
│   │   └── devcontainer.json
│   ├── src/
│   ├── Dockerfile.prod          # Production Dockerfile
│   ├── Dockerfile.dev           # Development Dockerfile (hot reload)
│   └── pom.xml
├── ConfigServer/                 # Configuration Server
│   ├── .devcontainer/           # VS Code/Cursor dev container config
│   │   └── devcontainer.json
│   ├── src/
│   ├── Dockerfile.prod          # Production Dockerfile
│   ├── Dockerfile.dev           # Development Dockerfile (hot reload)
│   └── pom.xml
├── CloudGateway/                 # API Gateway
│   ├── .devcontainer/           # VS Code/Cursor dev container config
│   │   └── devcontainer.json
│   ├── src/
│   ├── Dockerfile.prod          # Production Dockerfile
│   ├── Dockerfile.dev           # Development Dockerfile (hot reload)
│   └── pom.xml
├── ProductService/               # Product Microservice
│   ├── .devcontainer/           # VS Code/Cursor dev container config
│   │   └── devcontainer.json
│   ├── src/
│   ├── Dockerfile.prod          # Production Dockerfile
│   ├── Dockerfile.dev           # Development Dockerfile (hot reload + debug)
│   └── pom.xml
├── OrderService/                 # Order Microservice
│   ├── .devcontainer/           # VS Code/Cursor dev container config
│   │   └── devcontainer.json
│   ├── src/
│   ├── Dockerfile.prod          # Production Dockerfile
│   ├── Dockerfile.dev           # Development Dockerfile (hot reload + debug)
│   └── pom.xml
├── PaymentService/               # Payment Microservice
│   ├── .devcontainer/           # VS Code/Cursor dev container config
│   │   └── devcontainer.json
│   ├── src/
│   ├── Dockerfile.prod          # Production Dockerfile
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
├── docker-compose.yml            # Production stack (uses Dockerfile.prod)
├── docker-compose.dev.yml        # Development stack (uses Dockerfile.dev)
└── README.md
```

## Prerequisites

### For Dev Container Development (Recommended)
- **Docker** and **Docker Compose**
- **VS Code** or **Cursor** with [Dev Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)
- **Auth0 Account** (for OAuth2 authentication)

### For Local Development
- **Java 17** or higher
- **Maven 3.6+**
- **Docker** and **Docker Compose**
- **MySQL 8.0+** (optional - can use Docker)
- **Auth0 Account** (for OAuth2 authentication)

### For Kubernetes Deployment
- **Kubernetes** cluster (Minikube, GKE, EKS, AKS, etc.)
- **kubectl** configured

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd SpringBoot
```

### 2. Configure OAuth2 (Auth0)

This application uses **Auth0** for OAuth2 authentication and authorization. Auth0 provides:
- **API Gateway Authentication:** Validates JWT tokens for all incoming requests
- **Inter-Service Communication:** OAuth2 Client Credentials flow for service-to-service calls
- **Frontend User Login:** Authorization Code flow for browser-based authentication
- **Swagger UI Authentication:** Bearer token support in API documentation

#### Why Auth0?
- Industry-standard OAuth2/OIDC provider
- Easy integration with Spring Security
- Supports multiple authentication flows (Authorization Code, Client Credentials, etc.)
- Free tier with generous limits
- Excellent documentation and developer experience

#### Understanding Auth0 Application Types

⚠️ **IMPORTANT:** This microservices architecture requires **TWO different Auth0 applications**:

| Application Type | Used By | Purpose | Grant Types |
|-----------------|---------|---------|-------------|
| **Regular Web Application** | CloudGateway | Frontend user login (browser) | Authorization Code, Refresh Token |
| **Machine-to-Machine (M2M)** | OrderService, PaymentService, ProductService | Service-to-service API calls | Client Credentials |

**Why two applications?**
- **Regular Web Apps** support Authorization Code flow for user login but typically don't support Client Credentials
- **M2M Apps** support Client Credentials for service-to-service calls but don't support user login
- Using separate apps follows security best practices and principle of least privilege

#### Setting Up Auth0

##### Step 1: Create a Free Auth0 Account

1. Go to https://auth0.com/signup
2. Sign up for a free account
3. Note your Auth0 domain (e.g., `https://dev-xxxxxxxx.us.auth0.com`)

##### Step 2: Create API Resource

1. Navigate to **Applications** → **APIs** → **Create API**
2. Configure:
   - **Name:** SpringBoot Microservices API
   - **Identifier:** `http://springboot-microservices-api` (this is your audience)
   - **Signing Algorithm:** RS256
3. Click **Create**

##### Step 3: Create Regular Web Application (for CloudGateway)

1. Navigate to **Applications** → **Create Application**
2. Choose **Regular Web Applications**
3. Configure:
   - **Name:** SpringBoot Microservices Web
   - **Allowed Callback URLs:** `http://localhost:9090/login/oauth2/code/auth0`
   - **Allowed Logout URLs:** `http://localhost:9090`
   - **Allowed Web Origins:** `http://localhost:3000,http://localhost:4200,http://localhost:5173`
4. Go to **Settings** → **Advanced Settings** → **Grant Types**
   - Ensure enabled: ✅ Authorization Code, ✅ Refresh Token
   - **Optional:** ✅ Client Credentials (if you want to use ONE app for everything - see note below)
5. **Copy** the **Client ID** and **Client Secret** (you'll use these in CloudGateway)

##### Step 4: Create Machine-to-Machine Application (for Backend Services)

**Note:** Skip this if you enabled Client Credentials grant in Step 3

1. Navigate to **Applications** → **Create Application**
2. Choose **Machine to Machine Applications**
3. Configure:
   - **Name:** SpringBoot Microservices M2M
   - **Authorize:** Select "SpringBoot Microservices API"
   - **Permissions:** Select all scopes you need
4. **Copy** the **Client ID** and **Client Secret** (you'll use these in OrderService)

##### Step 5: Configure Environment Variables

Create a `.env` file (copy from `.env.example`):

**Option A: Using ONE Regular Web App (if Client Credentials is enabled):**
```bash
AUTH0_CLIENT_ID=<your_web_app_client_id>
AUTH0_CLIENT_SECRET=<your_web_app_client_secret>
AUTH0_AUDIENCE=http://springboot-microservices-api
AUTH0_ISSUER_URI=https://dev-xxxxxxxx.us.auth0.com/
```

**Option B: Using TWO separate apps (recommended for better security):**
```bash
# CloudGateway (Regular Web App)
AUTH0_CLIENT_ID=<your_web_app_client_id>
AUTH0_CLIENT_SECRET=<your_web_app_client_secret>

# Backend Services (M2M App) - if using separate app
AUTH0_M2M_CLIENT_ID=<your_m2m_client_id>
AUTH0_M2M_CLIENT_SECRET=<your_m2m_client_secret>

# Shared
AUTH0_AUDIENCE=http://springboot-microservices-api
AUTH0_ISSUER_URI=https://dev-xxxxxxxx.us.auth0.com/
```

**If using Option B,** OrderService must use `AUTH0_M2M_CLIENT_ID` instead of `AUTH0_CLIENT_ID` (already configured in this project).

##### Which Service Uses Which Auth0 Application?

| Service | Auth0 Application Type | Environment Variables | Purpose |
|---------|----------------------|----------------------|---------|
| **CloudGateway** | Regular Web Application | `AUTH0_CLIENT_ID`<br>`AUTH0_CLIENT_SECRET` | Frontend user login (Authorization Code flow) |
| **OrderService** | Machine-to-Machine | `AUTH0_M2M_CLIENT_ID`<br>`AUTH0_M2M_CLIENT_SECRET` | Service-to-service calls (Client Credentials flow) |
| **ProductService** | N/A (resource server only) | `AUTH0_ISSUER_URI` | JWT token validation only |
| **PaymentService** | N/A (resource server only) | `AUTH0_ISSUER_URI` | JWT token validation only |

**Note:** All services share `AUTH0_ISSUER_URI` and `AUTH0_AUDIENCE` for consistent JWT validation.

#### Application Configuration

All sensitive credentials are externalized to environment variables. The configuration files already use environment variable placeholders:

**Cloud Gateway** (`CloudGateway/src/main/resources/application-dev.yml`):
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
            scope: openid,profile,email
        provider:
          auth0:
            issuer-uri: ${AUTH0_ISSUER_URI}
```

**Order Service** (`OrderService/src/main/resources/application-dev.yaml`):
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
            scope: openid,profile,email
            client-id: ${AUTH0_M2M_CLIENT_ID}      # Uses M2M app for service-to-service calls
            client-secret: ${AUTH0_M2M_CLIENT_SECRET}
        provider:
          auth0:
            issuer-uri: ${AUTH0_ISSUER_URI}
```

**Product Service & Payment Service** (`application-dev.yml`):
```yaml
spring:
  security:
    oauth2:
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
```

**⚠️ Security Best Practices:**
- ✅ All credentials use environment variables (no hardcoded values)
- ✅ `.env` file is in `.gitignore` (never committed)
- ✅ `.env.example` provides template without real credentials
- ✅ Docker Compose automatically loads `.env` file
- ✅ Separate applications for user login vs service-to-service (Option B)

#### Verifying Your Setup

After configuring `.env` and restarting services, verify both Auth0 applications are working correctly:

**1. Verify Environment Variables Are Loaded:**
```bash
# Check CloudGateway has Regular Web App credentials
docker-compose -f docker-compose.dev.yml exec cloudgateway env | grep AUTH0_CLIENT_ID

# Check OrderService has M2M credentials
docker-compose -f docker-compose.dev.yml exec orderservice env | grep AUTH0_M2M_CLIENT_ID

# Verify shared configuration
docker-compose -f docker-compose.dev.yml exec cloudgateway env | grep AUTH0_ISSUER_URI
```

**2. Test User Login (Regular Web App):**
```bash
# Open in browser - should redirect to Auth0 login
open http://localhost:9090/authenticate/login
```

**3. Test Service-to-Service Authentication (M2M App):**
```bash
# OrderService should automatically use M2M credentials when calling other services
# Check logs for successful token acquisition
docker-compose -f docker-compose.dev.yml logs orderservice | grep -i "oauth\|token"
```

**4. Check Service Health:**
```bash
# All services should be registered with Eureka
open http://localhost:8761

# Swagger UI should be accessible
open http://localhost:9090/swagger-ui.html
```

#### Getting an Access Token

**Method 1: User Login (Browser-based - using Regular Web App)**

```bash
# Open in browser (redirects to Auth0 login page):
http://localhost:9090/authenticate/login
```

After successful login, you'll receive a JSON response with:
```json
{
  "userId": "user@example.com",
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "v1.MRrT...",
  "expiresAt": 1759598400,
  "auhorityList": ["ROLE_USER", "SCOPE_openid", "SCOPE_profile", "SCOPE_email"]
}
```

**Method 2: Client Credentials (API-to-API - using M2M App)**

```bash
curl --request POST \
  --url https://dev-xxxxxxxx.us.auth0.com/oauth/token \
  --header 'content-type: application/json' \
  --data '{
    "client_id":"YOUR_M2M_CLIENT_ID",
    "client_secret":"YOUR_M2M_CLIENT_SECRET",
    "audience":"http://springboot-microservices-api",
    "grant_type":"client_credentials"
  }'
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

**Using the Token:**

```bash
curl --location 'http://localhost:9090/product/1' \
  --header 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...'
```

#### How Authentication Works

**Flow 1: Frontend User Login (Authorization Code Flow)**

1. **User visits:** `http://localhost:9090/authenticate/login`
2. **CloudGateway redirects** to Auth0 login page
3. **User authenticates** with Auth0 (email/password, social login, etc.)
4. **Auth0 redirects back** to CloudGateway with authorization code
5. **CloudGateway exchanges** code for access token, refresh token, and ID token
6. **Response returned** to user with tokens and user information

**Flow 2: API Request with Token**

1. **Client sends request** to CloudGateway: `Authorization: Bearer <access_token>`
2. **CloudGateway validates** JWT token signature using Auth0's public keys
3. **CloudGateway extracts** user claims (email, roles, scopes) from token
4. **CloudGateway routes** request to appropriate backend service
5. **Backend service validates** token independently (stateless)

**Flow 3: Service-to-Service Communication (Client Credentials Flow)**

1. **OrderService needs** to call ProductService/PaymentService
2. **RestTemplateInterceptor** intercepts outgoing HTTP request
3. **Automatically obtains** access token from Auth0 using Client Credentials grant
4. **Adds** `Authorization: Bearer <token>` header to request
5. **Token cached** and automatically refreshed when expired

**Architecture Diagram:**
```
Frontend (React/Vue)
    ↓ (1) GET /authenticate/login
CloudGateway (Port 9090)
    ↓ (2) Redirect to Auth0
Auth0 (dev-xxxxxxxx.us.auth0.com)
    ↓ (3) User authenticates
CloudGateway
    ↓ (4) Exchange code for tokens
CloudGateway → Frontend (returns tokens)
    ↓
Frontend → CloudGateway (API requests with Bearer token)
    ↓
CloudGateway → ProductService/OrderService/PaymentService
    ↓
OrderService → ProductService/PaymentService (with auto-injected OAuth2 token)
```

**Testing with Swagger UI:**

1. Access: http://localhost:9090/swagger-ui.html
2. Click **Authorize** button
3. Enter: `Bearer <your-access-token>` (from `/authenticate/login` response)
4. Test APIs interactively

#### Troubleshooting Auth0

**Common Issues and Solutions:**

##### 1. "Callback URL mismatch" Error

**Problem:** When accessing `/authenticate/login`, you see:
```
Oops!, something went wrong
Callback URL mismatch.
The provided redirect_uri is not in the list of allowed callback URLs.
```

**Solution:**
1. Go to: https://manage.auth0.com/dashboard/us/dev-5nw6367bfpr277ec/applications
2. Click on your application
3. In **"Allowed Callback URLs"** add:
   ```
   http://localhost:9090/login/oauth2/code/auth0
   ```
4. In **"Allowed Logout URLs"** add:
   ```
   http://localhost:9090
   ```
5. In **"Allowed Web Origins"** add:
   ```
   http://localhost:9090
   ```
6. Click **"Save Changes"**

---

##### 2. "Service not enabled within domain" Error

**Problem:** When requesting a token:
```json
{
  "error": "access_denied",
  "error_description": "Service not enabled within domain: http://localhost:9090"
}
```

**Root Cause:** `AUTH0_AUDIENCE` is set to your app URL instead of your API Identifier.

**Solution:**
1. Go to: https://manage.auth0.com/dashboard/us/dev-5nw6367bfpr277ec/apis
2. **If you have an API:** Click it and copy the **"Identifier"**
3. **If no API exists:** Create one:
   - Click **"Create API"**
   - **Name:** `Spring Boot Microservices API`
   - **Identifier:** `https://api.springboot.example.com` (can be any URI)
   - **Signing Algorithm:** `RS256`
   - Click **"Create"**
   - Copy the **"Identifier"**
4. Update `.env`:
   ```bash
   # WRONG:
   AUTH0_AUDIENCE=http://localhost:9090

   # CORRECT:
   AUTH0_AUDIENCE=https://api.springboot.example.com
   ```
5. Restart services:
   ```bash
   docker-compose -f docker-compose.dev.yml restart cloudgateway orderservice
   ```

---

##### 3. "Oops! Something went wrong" on /authenticate/login

**Problem:** Generic error on Auth0 login page.

**Root Cause:** You're using a **Machine-to-Machine (M2M)** application, which doesn't support interactive browser login.

**Solution A - Use M2M with Client Credentials (Recommended for APIs):**

The `/authenticate/login` endpoint is for user login. For API-to-API communication, use Client Credentials flow:

```bash
# Get access token
curl -s -X POST https://dev-5nw6367bfpr277ec.us.auth0.com/oauth/token \
  -H 'content-type: application/json' \
  -d '{
    "client_id":"YOUR_CLIENT_ID",
    "client_secret":"YOUR_CLIENT_SECRET",
    "audience":"https://api.springboot.example.com",
    "grant_type":"client_credentials"
  }'
```

Response:
```json
{
  "access_token": "eyJ...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

Use the token:
```bash
curl http://localhost:9090/product/1 \
  -H "Authorization: Bearer eyJ..."
```

**Solution B - Create Web Application (For user login):**

Only if you need interactive browser login:

1. Go to: https://manage.auth0.com/dashboard/us/dev-5nw6367bfpr277ec/applications
2. Click **"Create Application"**
3. **Name:** `Spring Boot Gateway Web`
4. **Type:** Select **"Regular Web Applications"**
5. Click **"Create"**
6. Configure:
   - **Allowed Callback URLs:** `http://localhost:9090/login/oauth2/code/auth0`
   - **Allowed Logout URLs:** `http://localhost:9090`
   - **Allowed Web Origins:** `http://localhost:9090`
   - **Grant Types:** ✅ Authorization Code, ✅ Refresh Token
7. Click **"Save Changes"**
8. Update `.env` with the new Client ID and Secret

---

##### 4. 401 Unauthorized on API Requests

**Causes:**
- Token is expired (check `expires_in` from token response)
- Wrong audience in token request
- Issuer URI mismatch
- Client ID/Secret incorrect

**Check token validity:**
```bash
# Decode JWT (header and payload only)
echo "YOUR_TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null | jq
```

**Verify configuration:**
```bash
# Check environment variables are loaded
docker-compose -f docker-compose.dev.yml exec cloudgateway env | grep AUTH0
```

**Review logs:**
```bash
docker-compose -f docker-compose.dev.yml logs --tail=100 cloudgateway | grep -i error
```

---

##### 5. "Failed to load API definition" in Swagger UI

**Problem:** Swagger UI loads at http://localhost:9090/swagger-ui.html but shows:
```
Failed to load API definition.
Errors: Unauthorized /order/api-docs
```

**Root Cause:** Gateway routes require authentication, blocking Swagger UI from fetching API docs.

**Workaround - Use Direct Service URLs:**

Instead of the centralized Swagger UI, access each service directly:

- **OrderService:** http://localhost:8082/swagger-ui.html
- **ProductService:** http://localhost:8081/swagger-ui.html
- **PaymentService:** http://localhost:8083/swagger-ui.html

These work without authentication because each service permits `/swagger-ui/**` and `/api-docs/**` paths.

**Full Solution (Advanced):**

Update CloudGateway to pass API docs requests without authentication (requires modifying security filters).

---

##### 6. Services Can't Communicate (Service-to-Service)

**Problem:** OrderService fails when calling ProductService/PaymentService.

**Check:**
1. **Environment variables set:**
   ```bash
   docker-compose -f docker-compose.dev.yml exec orderservice env | grep AUTH0
   ```

2. **Logs show token errors:**
   ```bash
   docker-compose -f docker-compose.dev.yml logs orderservice | grep -i "oauth\|token\|auth"
   ```

3. **Client credentials configured:**
   - Check `OrderService/src/main/resources/application-dev.yaml`
   - Verify `internal-client` registration exists
   - Confirm `AUTH0_CLIENT_ID` and `AUTH0_CLIENT_SECRET` are set

4. **API authorized for M2M app:**
   - Go to: https://manage.auth0.com/dashboard/us/dev-5nw6367bfpr277ec/applications
   - Click your M2M application
   - Go to **"APIs" tab**
   - Ensure your API is listed and authorized

---

##### 7. Environment Variables Not Loading

**Problem:** Services start but Auth0 variables are `${AUTH0_CLIENT_ID}` (not resolved).

**Cause:** Docker Compose needs to be recreated (not just restarted) to pick up new `.env` variables.

**Solution:**
```bash
# Stop and remove containers
docker-compose -f docker-compose.dev.yml down

# Recreate with updated .env
docker-compose -f docker-compose.dev.yml up -d

# Or force recreate specific service
docker-compose -f docker-compose.dev.yml up -d --force-recreate cloudgateway
```

**Verify variables loaded:**
```bash
docker-compose -f docker-compose.dev.yml config | grep AUTH0
```

---

##### 8. "Do I Need to Change My M2M App?" - Understanding Application Types

**Problem:** You have a Machine-to-Machine (M2M) application, but frontend login (`/authenticate/login`) doesn't work.

**Explanation:**

M2M applications **only support Client Credentials flow** (service-to-service API calls). They **do NOT support Authorization Code flow** (browser-based user login).

**Your system needs BOTH authentication flows:**

| Flow | Use Case | Auth0 App Type Required |
|------|----------|------------------------|
| **Authorization Code** | Frontend user login via browser | Regular Web Application |
| **Client Credentials** | Backend service-to-service calls | Machine-to-Machine Application |

**Solution - Choose One Option:**

**Option A: Enable Client Credentials on Regular Web App (Simpler)**

1. Go to your Regular Web Application in Auth0 Dashboard
2. Navigate to **Settings** → **Advanced Settings** → **Grant Types**
3. Enable: ✅ **Client Credentials** (in addition to Authorization Code, Refresh Token)
4. **Save Changes**
5. Use this **single app** for both CloudGateway AND OrderService

**Pros:**
- Only one application to manage
- Simpler `.env` configuration (single `AUTH0_CLIENT_ID`)

**Cons:**
- Less secure (same credentials used for user login and service calls)
- Violates principle of least privilege

**Option B: Use TWO Separate Applications (Recommended)**

1. **Keep** your Regular Web Application for CloudGateway (user login)
2. **Keep or create** your M2M Application for OrderService (service calls)
3. Update `.env` to include both:
   ```bash
   # CloudGateway uses Regular Web App
   AUTH0_CLIENT_ID=<web_app_client_id>
   AUTH0_CLIENT_SECRET=<web_app_client_secret>

   # OrderService uses M2M App
   AUTH0_M2M_CLIENT_ID=<m2m_client_id>
   AUTH0_M2M_CLIENT_SECRET=<m2m_client_secret>
   ```
4. Update OrderService configuration files to use `AUTH0_M2M_CLIENT_ID`:
   ```yaml
   # OrderService/src/main/resources/application-dev.yaml
   spring:
     security:
       oauth2:
         client:
           registration:
             internal-client:
               client-id: ${AUTH0_M2M_CLIENT_ID}
               client-secret: ${AUTH0_M2M_CLIENT_SECRET}
   ```

**Pros:**
- Better security (separate credentials for different purposes)
- Follows security best practices
- Easier to audit and revoke access

**Cons:**
- Two applications to manage
- Slightly more complex configuration

**Recommendation:** Use **Option B** for production deployments

#### Security Best Practices Implemented

This project implements several OAuth2 security best practices:

✅ **Environment Variable Configuration**
- All Auth0 credentials use environment variables (no hardcoded secrets)
- Configuration: `${AUTH0_CLIENT_ID}`, `${AUTH0_CLIENT_SECRET}`, etc.
- Set via `.env` file (automatically loaded by Docker Compose)
- No fallback defaults to prevent accidental exposure

✅ **Scope-Based Authorization**
- All services require `SCOPE_internal` for API access
- ProductService: `/product/**` endpoints
- OrderService: `/order/**` endpoints
- PaymentService: `/payment/**` endpoints
- Configuration: `WebSecurityConfig.java` in each service

✅ **Automatic Token Refresh**
- OrderService configured with `.refreshToken()` support
- Tokens automatically renewed before expiration
- No manual token management required
- Implementation: `OrderServiceApplication.java:58`

✅ **Proper Error Handling**
- OAuth2 interceptors throw exceptions on token failure
- No silent failures - all auth errors logged and reported
- Implementation: `RestTemplateInterceptor.java:38-44`

✅ **CORS Configuration**
- No wildcard origins with credentials
- Specific allowed origins for security
- Supports Gateway, frontend ports
- Configuration: `WebSecurityConfig.java` (OrderService)

✅ **Standardized Auth0 Domain**
- All services use same Auth0 tenant via `AUTH0_ISSUER_URI` environment variable
- Consistent JWT validation across microservices
- Single source of truth for authentication

#### Using Environment Variables

**Method 1: Create .env file** (Recommended)

```bash
# Copy example file
cp .env.example .env

# Edit .env with your values
OKTA_CLIENT_ID=your_actual_client_id
OKTA_CLIENT_SECRET=your_actual_client_secret
```

**Method 2: Export variables**

```bash
export OKTA_CLIENT_ID=your_actual_client_id
export OKTA_CLIENT_SECRET=your_actual_client_secret
docker-compose -f docker-compose.dev.yml up -d
```

**Method 3: Inline with docker-compose**

```bash
OKTA_CLIENT_ID=xxx OKTA_CLIENT_SECRET=yyy docker-compose -f docker-compose.dev.yml up -d
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

# Check API Documentation (Swagger UI)
open http://localhost:9090/swagger-ui.html

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

### Interactive API Documentation (Swagger UI)

The project uses **Springdoc OpenAPI** for interactive API documentation. All services expose OpenAPI documentation that can be accessed through the Cloud Gateway.

#### Centralized Swagger UI (Recommended)

Access all microservices APIs from a single interface through the Cloud Gateway:

**URL:** http://localhost:9090/swagger-ui.html

This provides:
- ✅ **All services in one place**: Product, Order, and Payment services
- ✅ **Try it out**: Interactive API testing directly from the browser
- ✅ **OAuth2 authentication**: Pre-configured with bearer token support
- ✅ **Request/Response examples**: Auto-generated from code
- ✅ **Schema definitions**: Complete data models

**Service Selection:**
- Use the dropdown in the top-right corner to switch between:
  - Product Service API
  - Order Service API
  - Payment Service API

#### Individual Service Documentation

Each service also exposes its own OpenAPI endpoints:

| Service | Swagger UI | OpenAPI JSON |
|---------|-----------|--------------|
| Product Service | http://localhost:8081/swagger-ui.html | http://localhost:8081/api-docs |
| Order Service | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs |
| Payment Service | http://localhost:8083/swagger-ui.html | http://localhost:8083/api-docs |

**Note:** When accessing individual service UIs, you may need to configure OAuth2 authentication manually.

#### Via Cloud Gateway

All service APIs are also available through the gateway:

| Service | OpenAPI JSON via Gateway |
|---------|-------------------------|
| Product Service | http://localhost:9090/product/api-docs |
| Order Service | http://localhost:9090/order/api-docs |
| Payment Service | http://localhost:9090/payment/api-docs |

### Manual API Testing

This section provides example `curl` commands for testing the APIs manually.

#### Base URL
The base URL for all API requests is:
```
http://localhost:9090
```

#### Authentication
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

The project includes multiple development environment options to suit different workflows.

### Development Modes

#### Mode 0: Dev Containers (VS Code/Cursor) - **Easiest Setup** ⭐

Each service has its own `.devcontainer` configuration for seamless container-based development:

```
SpringBoot/
├── ProductService/.devcontainer/       # ProductService dev container
├── OrderService/.devcontainer/         # OrderService dev container
├── PaymentService/.devcontainer/       # PaymentService dev container
├── ServiceRegistry/.devcontainer/      # ServiceRegistry dev container
├── ConfigServer/.devcontainer/         # ConfigServer dev container
└── CloudGateway/.devcontainer/         # CloudGateway dev container
```

**How to use:**

1. **Install:** [Dev Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) in VS Code/Cursor

2. **Open a service folder** (e.g., `ProductService/`)

3. **Reopen in Container:** VS Code will detect `.devcontainer/devcontainer.json` and prompt you to reopen in container

4. **Start coding!** All dependencies and services run automatically:
   - ✅ Your service runs in its container with hot reload
   - ✅ All other services (ServiceRegistry, ConfigServer, databases) run as dependencies
   - ✅ Full Java tooling, IntelliJ IDEA keybindings, Spring Boot support
   - ✅ Debug ports exposed and ready to use
   - ✅ Code changes reload automatically

**Benefits:**
- 🚀 **Zero setup**: No local Java, Maven, or MySQL installation required
- 🔥 **Hot reload**: Code changes reload automatically
- 🐛 **Debugging**: Remote debugging pre-configured
- 🎯 **Focused**: Work on one service at a time
- 📦 **Consistent**: Same environment for all developers
- 🔄 **Isolated**: Each service in its own container

**Service-specific ports forwarded:**

| Service | Main Port | Debug Port | Database Port |
|---------|-----------|------------|---------------|
| ProductService | 8081 | 5005 | 3306 |
| OrderService | 8082 | 5006 | 3307 |
| PaymentService | 8083 | 5007 | 3308 |
| ServiceRegistry | 8761 | - | - |
| ConfigServer | 9296 | - | - |
| CloudGateway | 9090 | - | - |

**All containers also forward**: 8761 (Eureka), 9090 (Gateway), 9296 (Config)

#### Mode 1: Hybrid Development

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

### Dockerfiles Explained

The project has **two Dockerfiles per service** for different environments:

#### `Dockerfile.prod` (Production)
```dockerfile
FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} productservice.jar
ENTRYPOINT ["java", "-jar", "/productservice.jar"]
```
**Used by:** `docker-compose.yml`

**Features:**
- ✅ Small image size (~150MB with Alpine)
- ✅ Fast startup
- ✅ Optimized for production
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
**Used by:** `docker-compose.dev.yml`

**Features:**
- ✅ **Hot reload** with Spring Boot DevTools
- ✅ **Volume mounts** for instant code changes
- ✅ **Remote debugging** enabled
- ✅ No rebuild needed for code changes
- ⚠️ Larger image (~700MB)
- ⚠️ Slower initial build

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
- All services automatically use `dev` profile (configured via `SPRING_PROFILES_ACTIVE=dev` in `docker-compose.dev.yml`)

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
- **Dev Containers:** Each service has its own `.devcontainer` configuration for VS Code/Cursor
- **Spring Profiles:** `docker-compose.dev.yml` automatically activates `dev` profile for all services via `SPRING_PROFILES_ACTIVE=dev`

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

### API Documentation URLs

| Resource | URL | Description |
|----------|-----|-------------|
| **Centralized Swagger UI** | http://localhost:9090/swagger-ui.html | All services in one interface (Recommended) |
| Product Service Swagger | http://localhost:8081/swagger-ui.html | Product Service only |
| Order Service Swagger | http://localhost:8082/swagger-ui.html | Order Service only |
| Payment Service Swagger | http://localhost:8083/swagger-ui.html | Payment Service only |
| Product Service OpenAPI | http://localhost:9090/product/api-docs | OpenAPI JSON spec |
| Order Service OpenAPI | http://localhost:9090/order/api-docs | OpenAPI JSON spec |
| Payment Service OpenAPI | http://localhost:9090/payment/api-docs | OpenAPI JSON spec |

### Auth0 Resources

| Resource | URL | Description |
|----------|-----|-------------|
| Auth0 Signup | https://auth0.com/signup | Sign up for free account |
| Auth0 Dashboard | https://manage.auth0.com/dashboard | Manage applications, APIs, users |
| Token Endpoint | https://YOUR-DOMAIN.auth0.com/oauth/token | Get access tokens (Client Credentials) |
| User Info Endpoint | https://YOUR-DOMAIN.auth0.com/userinfo | Get user profile information |
| JWKS Endpoint | https://YOUR-DOMAIN.auth0.com/.well-known/jwks.json | Public keys for JWT verification |
| OpenID Configuration | https://YOUR-DOMAIN.auth0.com/.well-known/openid-configuration | OAuth2/OIDC metadata |
| Auth0 Documentation | https://auth0.com/docs | Complete developer documentation |

### Complete Setup Commands

**Option 1: Automated (Recommended)**
```bash
# 1. Clone and navigate
git clone <repository-url>
cd SpringBoot

# 2. Configure Auth0 OAuth2 (create .env file - see "Configure OAuth2 (Auth0)" section)

# 3. Build and start everything with one command
./build-and-start.sh

# 4. Verify
open http://localhost:8761  # Check Eureka - all services should be registered
open http://localhost:9090/swagger-ui.html  # Interactive API documentation

# 5. Test API (use Swagger UI or curl - see API Documentation section)
curl http://localhost:9090/product/1 \
  -H 'Authorization: Bearer <your-token>'
```

**Option 2: Manual**
```bash
# 1. Clone and navigate
git clone <repository-url>
cd SpringBoot

# 2. Configure Auth0 OAuth2 (create .env file - see "Configure OAuth2 (Auth0)" section)

# 3. Build JAR files and Docker images
mvn clean package -DskipTests
docker-compose build

# 4. Start all services
docker-compose up -d

# 5. Verify
docker-compose ps
open http://localhost:8761  # Check Eureka - all services should be registered
open http://localhost:9090/swagger-ui.html  # Interactive API documentation

# 6. Test API (use Swagger UI or curl - see API Documentation section)
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
