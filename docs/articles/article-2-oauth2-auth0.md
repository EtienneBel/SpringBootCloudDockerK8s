# Mastering Microservices with Spring Boot, Spring Security and OAuth2 with Auth0 (Part 2 - Updated 2025)

> Complete guide to securing Spring Boot microservices with OAuth2, JWT tokens, and Auth0 authentication provider.

---

## Introduction

In Part 1, we built a microservices application with service discovery, API gateway, and circuit breakers. Now we'll secure it with OAuth2 authentication using **Auth0** as our identity provider.

**What You'll Learn:**
- ✅ OAuth2 fundamentals and flows
- ✅ Setting up Auth0 (Regular Web App + M2M)
- ✅ JWT token validation at API Gateway
- ✅ Service-to-service authentication with Client Credentials flow
- ✅ CORS configuration for frontend applications
- ✅ Automatic OAuth2 token injection with interceptors

**Prerequisites:**
- Completed Part 1 (microservices setup)
- Free Auth0 account: https://auth0.com/signup

**GitHub Repository:** [Your Repository Link]

---

## Table of Contents

1. [OAuth2 Fundamentals](#oauth2-fundamentals)
2. [Auth0 Setup Guide](#auth0-setup-guide)
3. [Securing the API Gateway](#securing-the-api-gateway)
4. [Securing Microservices](#securing-microservices)
5. [Service-to-Service Authentication](#service-to-service-authentication)
6. [Frontend Integration](#frontend-integration)
7. [Testing Authentication](#testing-authentication)
8. [Troubleshooting](#troubleshooting)

---

## OAuth2 Fundamentals

### What is OAuth2?

OAuth 2.0 is an **authorization framework** that enables applications to obtain limited access to user accounts without exposing passwords. It delegates authentication to the service hosting the user account (Auth0 in our case).

### Why Auth0?

- ✅ **Managed Identity Provider** - No need to build authentication
- ✅ **Multiple Login Methods** - Email/password, social, enterprise SSO
- ✅ **Built-in Security** - MFA, breach detection, bot detection
- ✅ **JWT Tokens** - Stateless, scalable authentication
- ✅ **Free Tier** - 7,000 active users, unlimited logins

### OAuth2 Flows Used

Our microservices architecture uses **two OAuth2 flows**:

#### 1. Authorization Code Flow (User Login)
**Use Case:** Frontend users logging into the application

```
User → Login Button → API Gateway → Auth0 Login Page
                                       ↓
                              User enters credentials
                                       ↓
                              Auth0 validates user
                                       ↓
                        Redirect with authorization code
                                       ↓
                     Gateway exchanges code for tokens
                                       ↓
                   Access Token + Refresh Token + ID Token
```

#### 2. Client Credentials Flow (Service-to-Service)
**Use Case:** OrderService calling ProductService

```
OrderService → OAuth2 Interceptor
                      ↓
              Check if token valid
                      ↓
        Token expired? Request new token
                      ↓
        POST /oauth/token to Auth0
                      ↓
  client_id + client_secret authentication
                      ↓
           Access token returned
                      ↓
   Add "Authorization: Bearer <token>" header
                      ↓
          ProductService validates token
```

---

## Auth0 Setup Guide

### Step 1: Create Auth0 Account

1. Go to https://auth0.com/signup
2. Sign up with email or Google
3. Choose region (US/EU/AU)
4. Note your **Auth0 Domain**: `dev-xxxxxxx.us.auth0.com`

### Step 2: Create an API

This API represents your backend microservices.

1. Navigate to **Applications → APIs**
2. Click **Create API**

**Configuration:**
```
Name: SpringBoot Microservices API
Identifier: http://springboot-microservices-api
Signing Algorithm: RS256
```

3. Click **Create**
4. Note the **Identifier** (this is your `AUTH0_AUDIENCE`)

### Step 3: Create Regular Web Application

This application is for user authentication via the browser.

1. Navigate to **Applications → Applications**
2. Click **Create Application**

**Configuration:**
```
Name: SpringBoot Microservices Web
Application Type: Regular Web Applications
```

3. Click **Create**
4. Go to **Settings** tab

**Configure URLs:**
```
Allowed Callback URLs:
  http://localhost:9090/login/oauth2/code/auth0

Allowed Logout URLs:
  http://localhost:9090

Allowed Web Origins:
  http://localhost:3000,http://localhost:4200,http://localhost:5173
```

5. Scroll down to **Advanced Settings → Grant Types**
6. Ensure these are enabled:
   - ✅ Authorization Code
   - ✅ Refresh Token
   - ✅ Implicit (optional)

7. Click **Save Changes**

8. Copy these values:
   - **Client ID** → `AUTH0_CLIENT_ID`
   - **Client Secret** → `AUTH0_CLIENT_SECRET`

### Step 4: Create Machine-to-Machine (M2M) Application

This application is for service-to-service authentication.

1. Navigate to **Applications → Applications**
2. Click **Create Application**

**Configuration:**
```
Name: SpringBoot Microservices M2M
Application Type: Machine to Machine Applications
```

3. Select **SpringBoot Microservices API** (the API we created)
4. Click **Authorize**
5. Select **All** permissions (or specific scopes)
6. Click **Authorize**

7. Go to **Settings** tab
8. Copy these values:
   - **Client ID** → `AUTH0_M2M_CLIENT_ID`
   - **Client Secret** → `AUTH0_M2M_CLIENT_SECRET`

### Step 5: Environment Variables Setup

Create `.env` file in project root:

```bash
# Copy from .env.example
cp .env.example .env
```

**Edit `.env` with your Auth0 values:**

```properties
# ==========================================
# Auth0 Configuration
# ==========================================

# Regular Web Application (CloudGateway)
AUTH0_CLIENT_ID=your_web_app_client_id_here
AUTH0_CLIENT_SECRET=your_web_app_client_secret_here

# Machine-to-Machine Application (OrderService)
AUTH0_M2M_CLIENT_ID=your_m2m_client_id_here
AUTH0_M2M_CLIENT_SECRET=your_m2m_client_secret_here

# API Configuration
AUTH0_AUDIENCE=http://springboot-microservices-api

# Auth0 Domain (include trailing slash)
AUTH0_ISSUER_URI=https://dev-xxxxxxx.us.auth0.com/
```

### Step 6: Test Auth0 Connection

Run this command to verify your M2M credentials:

```bash
curl --request POST \
  --url https://dev-xxxxxxx.us.auth0.com/oauth/token \
  --header 'content-type: application/json' \
  --data '{
    "client_id":"YOUR_M2M_CLIENT_ID",
    "client_secret":"YOUR_M2M_CLIENT_SECRET",
    "audience":"http://springboot-microservices-api",
    "grant_type":"client_credentials"
  }'
```

**Expected Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IkR...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

✅ If you get an access token, Auth0 is configured correctly!

---

## Securing the API Gateway

The API Gateway validates JWT tokens for all incoming requests.

### Step 1: Add Dependencies

**pom.xml (CloudGateway)**
```xml
<dependencies>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- OAuth2 Resource Server (JWT validation) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- OAuth2 Client (Authorization Code flow) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>

    <!-- Reactive support (for WebFlux Gateway) -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-oauth2-jose</artifactId>
    </dependency>
</dependencies>
```

### Step 2: Configure Auth0 in application.yml

**CloudGateway/src/main/resources/application-dev.yml**
```yaml
spring:
  application:
    name: API-GATEWAY

  security:
    oauth2:
      # JWT Validation (Resource Server)
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
          audiences: ${AUTH0_AUDIENCE}

      # OAuth2 Client (Authorization Code flow)
      client:
        registration:
          auth0:
            client-id: ${AUTH0_CLIENT_ID}
            client-secret: ${AUTH0_CLIENT_SECRET}
            scope: openid,profile,email,offline_access
        provider:
          auth0:
            issuer-uri: ${AUTH0_ISSUER_URI}
```

**What This Does:**
- **Resource Server**: Validates JWT tokens in `Authorization` header
- **OAuth2 Client**: Handles user login via Authorization Code flow
- **Issuer URI**: Auth0's OAuth2 server endpoint
- **Audiences**: Expected `aud` claim in JWT token

### Step 3: Security Configuration

**CloudGateway/security/Auth0OAuth2WebSecurity.java**
```java
package com.ebelemgnegre.CloudGateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class Auth0OAuth2WebSecurity {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints (no authentication required)
                        .pathMatchers(
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/webjars/**",
                                "/authenticate/**"
                        ).permitAll()

                        // All other requests require authentication
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // Enable OAuth2 login via Auth0
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt()  // Validate JWT tokens
                )
                .csrf(csrf -> csrf.disable())  // Disable CSRF for APIs
                .build();
    }
}
```

**Security Rules:**
- `/actuator/**` - Public (health checks)
- `/swagger-ui/**` - Public (API docs)
- `/authenticate/**` - Public (login endpoints)
- All other routes - **Require JWT token**

### Step 4: CORS Configuration

Allow frontend applications to call the API Gateway.

**CloudGateway/config/CorsConfiguration.java**
```java
package com.ebelemgnegre.CloudGateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfiguration {

    @Bean
    public CorsWebFilter corsWebFilter() {
        org.springframework.web.cors.CorsConfiguration config =
            new org.springframework.web.cors.CorsConfiguration();

        // Allow frontend origins
        config.addAllowedOrigin("http://localhost:3000");   // React
        config.addAllowedOrigin("http://localhost:4200");   // Angular
        config.addAllowedOrigin("http://localhost:5173");   // Vite

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Allow all HTTP methods
        config.addAllowedMethod("*");

        // Allow all headers
        config.addAllowedHeader("*");

        // Expose Authorization header to frontend
        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
```

---

## Securing Microservices

Each backend service (Product, Order, Payment) validates JWT tokens independently.

### Step 1: Add OAuth2 Dependencies

**pom.xml (ProductService, OrderService, PaymentService)**
```xml
<dependencies>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- OAuth2 Resource Server -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
</dependencies>
```

### Step 2: Configure Auth0

**ProductService/src/main/resources/application-dev.yml**
```yaml
spring:
  application:
    name: PRODUCT-SERVICE

  security:
    oauth2:
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
          audiences: ${AUTH0_AUDIENCE}
```

**Repeat for OrderService and PaymentService.**

### Step 3: Security Configuration

**ProductService/security/SecurityConfig.java**
```java
package com.ebelemgnegre.ProductService.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints
                        .requestMatchers(
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/api-docs/**"
                        ).permitAll()

                        // All other endpoints require JWT token
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt()  // Validate JWT tokens
                )
                .csrf(csrf -> csrf.disable())
                .build();
    }
}
```

**Repeat for OrderService and PaymentService.**

---

## Service-to-Service Authentication

OrderService needs to call ProductService and PaymentService using **OpenFeign** with **Client Credentials flow** for automatic token injection.

### Architecture

```
OrderService
     │
     ├─ Need to call ProductService
     │
     ▼
OpenFeign Client (with OAuth2 interceptor)
     │
     ├─ Check if token exists and is valid
     │
     ▼
Token expired?
     │
     ├─ YES → Request new token from Auth0
     │         POST /oauth/token
     │         grant_type: client_credentials
     │         client_id: M2M_CLIENT_ID
     │         client_secret: M2M_CLIENT_SECRET
     │
     ▼
Add Authorization header
     │
     └─ Authorization: Bearer eyJhbGci...
     │
     ▼
ProductService receives request
     │
     └─ Validates JWT token
```

### Step 1: Configure M2M Client

**OrderService/src/main/resources/application-dev.yml**
```yaml
spring:
  application:
    name: ORDER-SERVICE

  security:
    oauth2:
      # Validate incoming JWT tokens
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
          audiences: ${AUTH0_AUDIENCE}

      # Client for making authenticated requests to other services
      client:
        registration:
          internal-client:
            provider: auth0
            authorization-grant-type: client_credentials
            scope: openid,profile,email
            client-id: ${AUTH0_M2M_CLIENT_ID}
            client-secret: ${AUTH0_M2M_CLIENT_SECRET}
        provider:
          auth0:
            issuer-uri: ${AUTH0_ISSUER_URI}
            token-uri: ${AUTH0_ISSUER_URI}oauth/token

  # Configure OpenFeign to use OAuth2
  cloud:
    openfeign:
      oauth2:
        clientRegistrationId: internal-client
        audience: ${AUTH0_AUDIENCE}
```

### Step 2: Configure OpenFeign Clients

OpenFeign automatically handles OAuth2 token injection when configured properly.

**OrderService/external/client/ProductService.java**
```java
package com.ebelemgnegre.OrderService.external.client;

import com.ebelemgnegre.OrderService.external.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductService {

    @PutMapping("/api/products/reduceQuantity/{id}")
    ResponseEntity<Void> reduceQuantity(
        @PathVariable("id") long productId,
        @RequestParam long quantity
    );

    @GetMapping("/api/products/{id}")
    ResponseEntity<ProductResponse> getProductById(@PathVariable("id") long productId);
}
```

**OrderService/external/client/PaymentService.java**
```java
package com.ebelemgnegre.OrderService.external.client;

import com.ebelemgnegre.OrderService.external.request.PaymentRequest;
import com.ebelemgnegre.OrderService.external.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentService {

    @PostMapping("/api/payments")
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);

    @GetMapping("/api/payments/order/{orderId}")
    ResponseEntity<PaymentResponse> getPaymentDetailsByOrderId(@PathVariable("orderId") long orderId);
}
```

### Step 3: Enable Feign Clients

**OrderService/OrderServiceApplication.java**
```java
package com.ebelemgnegre.OrderService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

### How It Works

1. **OrderService** needs to call ProductService via Feign client
2. **OpenFeign OAuth2 interceptor** automatically intercepts the HTTP request
3. Checks if token exists and is valid using configured `clientRegistrationId`
4. If token is expired:
   - Requests new token from Auth0
   - Uses M2M credentials (client_id + client_secret)
   - Receives new access token
5. **Interceptor** automatically adds `Authorization: Bearer <token>` header
6. **ProductService** receives request with valid JWT token
7. **ProductService** validates token and processes request

**Benefits:**
- ✅ **Declarative** - No manual interceptor code needed
- ✅ **Automatic token management** - Built-in token handling
- ✅ **Token caching** - Reuses valid tokens
- ✅ **Automatic refresh** - Requests new token when expired
- ✅ **Type-safe** - Compile-time checking with Feign interfaces
- ✅ **Transparent** - Business logic doesn't handle auth

---

## Frontend Integration

### React Example

**Install Dependencies**
```bash
npm install axios
```

**Login Flow**
```javascript
// src/auth/Login.js
import React from 'react';

const Login = () => {
  const handleLogin = () => {
    // Redirect to API Gateway OAuth2 endpoint
    window.location.href = 'http://localhost:9090/oauth2/authorization/auth0';
  };

  return (
    <div>
      <h1>Login</h1>
      <button onClick={handleLogin}>
        Login with Auth0
      </button>
    </div>
  );
};

export default Login;
```

**API Calls with Token**
```javascript
// src/api/api.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:9090',
});

// Add token to all requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;
```

**Fetch Products**
```javascript
// src/components/ProductList.js
import React, { useEffect, useState } from 'react';
import api from '../api/api';

const ProductList = () => {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await api.get('/api/products');
        setProducts(response.data);
      } catch (error) {
        console.error('Error fetching products:', error);
      }
    };

    fetchProducts();
  }, []);

  return (
    <div>
      <h1>Products</h1>
      {products.map((product) => (
        <div key={product.productId}>
          <h3>{product.productName}</h3>
          <p>Price: ${product.price}</p>
          <p>Stock: {product.quantity}</p>
        </div>
      ))}
    </div>
  );
};

export default ProductList;
```

---

## Testing Authentication

### Test 1: Public Endpoint (No Token Required)

```bash
curl http://localhost:9090/actuator/health
```

**Expected:** 200 OK
```json
{"status":"UP"}
```

### Test 2: Protected Endpoint (No Token)

```bash
curl http://localhost:9090/api/products
```

**Expected:** 401 Unauthorized
```json
{
  "error": "unauthorized",
  "error_description": "Full authentication is required to access this resource"
}
```

### Test 3: Get M2M Token

```bash
curl --request POST \
  --url https://dev-xxxxxxx.us.auth0.com/oauth/token \
  --header 'content-type: application/json' \
  --data '{
    "client_id":"YOUR_M2M_CLIENT_ID",
    "client_secret":"YOUR_M2M_CLIENT_SECRET",
    "audience":"http://springboot-microservices-api",
    "grant_type":"client_credentials"
  }' | jq -r '.access_token'
```

**Save the token:**
```bash
TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6..."
```

### Test 4: Protected Endpoint (With Token)

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:9090/api/products
```

**Expected:** 200 OK
```json
[
  {
    "productId": 1,
    "productName": "iPhone 15",
    "price": 999.99,
    "quantity": 100
  }
]
```

### Test 5: Decode JWT Token

Visit https://jwt.io and paste your token.

**Expected Claims:**
```json
{
  "iss": "https://dev-xxxxxxx.us.auth0.com/",
  "sub": "YOUR_M2M_CLIENT_ID@clients",
  "aud": "http://springboot-microservices-api",
  "iat": 1705315200,
  "exp": 1705401600,
  "scope": "openid profile email"
}
```

---

## Troubleshooting

### Issue 1: 401 Unauthorized with Valid Token

**Cause:** Audience mismatch

**Solution:** Verify `AUTH0_AUDIENCE` matches your API Identifier in Auth0

```bash
# Check token audience
echo $TOKEN | cut -d'.' -f2 | base64 -d | jq .aud

# Should match
echo $AUTH0_AUDIENCE
```

### Issue 2: Service-to-Service Calls Fail

**Cause:** M2M application not authorized for API

**Solution:**
1. Go to Auth0 Dashboard → Applications → M2M App
2. Click **APIs** tab
3. Ensure your API is authorized
4. Toggle permissions to "All"

### Issue 3: CORS Errors in Frontend

**Cause:** Origin not allowed

**Solution:** Add your frontend URL to `CorsConfiguration.java`:
```java
config.addAllowedOrigin("http://localhost:3000");
```

### Issue 4: Token Expired

**Cause:** Access token has 24-hour expiration

**Solution:** Use refresh token to get new access token:
```bash
curl --request POST \
  --url https://dev-xxxxxxx.us.auth0.com/oauth/token \
  --header 'content-type: application/json' \
  --data '{
    "grant_type":"refresh_token",
    "client_id":"YOUR_CLIENT_ID",
    "client_secret":"YOUR_CLIENT_SECRET",
    "refresh_token":"YOUR_REFRESH_TOKEN"
  }'
```

---

## Security Best Practices

✅ **Never commit secrets** - Use environment variables
✅ **Use HTTPS in production** - Encrypt tokens in transit
✅ **Validate tokens on every service** - Don't trust the gateway alone
✅ **Set token expiration** - Access tokens should be short-lived (24h)
✅ **Use refresh tokens** - For long-lived sessions
✅ **Enable MFA** - Multi-factor authentication in Auth0
✅ **Rotate secrets** - Change client secrets periodically
✅ **Monitor token usage** - Check Auth0 logs for anomalies

---

## Next Steps

In Part 3, we'll cover:
- 🐳 **Docker containerization** with multi-stage builds
- ☸️ **Kubernetes deployment** with ConfigMaps and Secrets
- 🚀 **CI/CD pipeline** with GitHub Actions
- 📊 **Monitoring** with Prometheus and Grafana

---

## Conclusion

You've successfully secured your microservices with:
- ✅ OAuth2 authentication with Auth0
- ✅ JWT token validation at Gateway and Services
- ✅ Service-to-service authentication with Client Credentials
- ✅ CORS configuration for frontends
- ✅ Automatic token management with interceptors

Your application now has enterprise-grade security!

**Source Code:** [GitHub Repository Link]

**Questions?** Leave a comment or reach out on LinkedIn!

---

**Author:** Etienne Belem Gnegre
**Date:** January 2025
**Tags:** #OAuth2 #Auth0 #SpringSecurity #JWT #Microservices #SpringBoot

---

## Additional Resources

- [Auth0 Documentation](https://auth0.com/docs)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [JWT.io](https://jwt.io) - Decode JWT tokens
- [OAuth 2.0 RFC](https://datatracker.ietf.org/doc/html/rfc6749)
- [OpenID Connect Spec](https://openid.net/connect/)
