# OAuth2 Authentication with Auth0

## Overview

**Pattern**: API Gateway Authentication with OAuth2/OIDC
**Provider**: Auth0
**Protocols**: OAuth 2.0, OpenID Connect (OIDC)
**Token Type**: JWT (JSON Web Tokens)

## What is OAuth2?

OAuth 2.0 is an authorization framework that enables applications to obtain limited access to user accounts on an HTTP service. It works by delegating user authentication to the service that hosts the user account and authorizing third-party applications to access that user account.

## Architecture

```
┌──────────────┐
│   Frontend   │
│  (React/Vue) │
└──────┬───────┘
       │ (1) Login Request
       ▼
┌─────────────────────────────┐
│   CloudGateway (9090)       │
│  Regular Web Application    │
│  Authorization Code Flow    │
└──────┬──────────────────────┘
       │ (2) Redirect to Auth0
       ▼
┌─────────────────────────────┐
│   Auth0                     │
│  https://dev-xxx.auth0.com  │
│  - User Authentication      │
│  - Token Issuance           │
└──────┬──────────────────────┘
       │ (3) Authorization Code
       ▼
┌─────────────────────────────┐
│   CloudGateway              │
│  Exchange code for tokens   │
└──────┬──────────────────────┘
       │ (4) Access Token
       │     Refresh Token
       │     ID Token
       ▼
┌──────────────┐
│   Frontend   │
│  (Tokens stored)
└──────┬───────┘
       │ (5) API Request
       │     Authorization: Bearer <token>
       ▼
┌─────────────────────────────┐
│   CloudGateway              │
│  - Validate JWT             │
│  - Check expiration         │
│  - Verify signature         │
└──────┬──────────────────────┘
       │ (6) Validated Request
       ▼
┌─────────────────────────────┐
│   ProductService            │
│   OrderService              │
│   PaymentService            │
│  (Also validate JWT)        │
└─────────────────────────────┘
```

## Auth0 Application Types

This project uses **TWO** different Auth0 applications for better security:

### 1. Regular Web Application

**Used By**: CloudGateway
**Purpose**: Frontend user login (browser-based)
**Grant Types**: Authorization Code + Refresh Token
**Flow**: Authorization Code Flow

```
User → Login Button → CloudGateway → Auth0 Login Page
                                    ↓
                           User authenticates
                                    ↓
                           Redirect with code
                                    ↓
                  CloudGateway exchanges code for tokens
                                    ↓
                          Access Token + Refresh Token
```

**Configuration**:
```
Application Type: Regular Web Application
Name: SpringBoot Microservices Web
Allowed Callback URLs: http://localhost:9090/login/oauth2/code/auth0
Allowed Logout URLs: http://localhost:9090
Allowed Web Origins: http://localhost:3000,http://localhost:4200,http://localhost:5173
Grant Types: ✅ Authorization Code, ✅ Refresh Token
```

### 2. Machine-to-Machine (M2M) Application

**Used By**: OrderService, PaymentService, ProductService
**Purpose**: Service-to-service API calls
**Grant Types**: Client Credentials
**Flow**: Client Credentials Flow

```
OrderService → Need to call ProductService
             ↓
   OAuth2 Interceptor checks token cache
             ↓
   Token expired? Request new token from Auth0
             ↓
   Auth0 validates client_id + client_secret
             ↓
   Returns access token (no user context)
             ↓
   Add Authorization header to request
             ↓
   ProductService validates token
```

**Configuration**:
```
Application Type: Machine to Machine
Name: SpringBoot Microservices M2M
Authorized API: SpringBoot Microservices API
Grant Types: ✅ Client Credentials
```

## OAuth2 Flows Implemented

### Flow 1: Authorization Code Flow (User Login)

**Use Case**: Frontend user wants to log in

**Steps**:

1. **User Initiates Login**:
```javascript
// Frontend
window.location.href = 'http://localhost:9090/authenticate/login';
```

2. **CloudGateway Redirects to Auth0**:
```
HTTP/1.1 302 Found
Location: https://dev-xxx.auth0.com/authorize?
  client_id=YOUR_CLIENT_ID&
  redirect_uri=http://localhost:9090/login/oauth2/code/auth0&
  response_type=code&
  scope=openid+profile+email+offline_access
```

3. **User Authenticates with Auth0**:
- User enters email/password
- Or uses social login (Google, Facebook, etc.)
- Or uses enterprise SSO

4. **Auth0 Redirects Back with Code**:
```
HTTP/1.1 302 Found
Location: http://localhost:9090/login/oauth2/code/auth0?code=AUTH_CODE
```

5. **CloudGateway Exchanges Code for Tokens**:
```bash
POST https://dev-xxx.auth0.com/oauth/token
Content-Type: application/json

{
  "grant_type": "authorization_code",
  "code": "AUTH_CODE",
  "redirect_uri": "http://localhost:9090/login/oauth2/code/auth0",
  "client_id": "YOUR_CLIENT_ID",
  "client_secret": "YOUR_CLIENT_SECRET"
}
```

6. **Auth0 Returns Tokens**:
```json
{
  "access_token": "eyJhbGci...",
  "refresh_token": "v1.MRrT...",
  "id_token": "eyJhbGci...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

7. **CloudGateway Returns to Frontend**:
```json
{
  "userId": "user@example.com",
  "accessToken": "eyJhbGci...",
  "refreshToken": "v1.MRrT...",
  "expiresAt": 1759750871,
  "auhorityList": ["ROLE_USER", "SCOPE_openid", "SCOPE_profile", "SCOPE_email"]
}
```

### Flow 2: Client Credentials Flow (Service-to-Service)

**Use Case**: OrderService needs to call ProductService

**Steps**:

1. **OrderService Needs to Make Request**:
```java
// RestTemplate interceptor automatically handles this
Product product = restTemplate.getForObject(
    "lb://PRODUCT-SERVICE/product/1",
    Product.class
);
```

2. **OAuth2 Interceptor Requests Token**:
```bash
POST https://dev-xxx.auth0.com/oauth/token
Content-Type: application/json

{
  "grant_type": "client_credentials",
  "client_id": "YOUR_M2M_CLIENT_ID",
  "client_secret": "YOUR_M2M_CLIENT_SECRET",
  "audience": "http://springboot-microservices-api"
}
```

3. **Auth0 Returns Access Token**:
```json
{
  "access_token": "eyJhbGci...",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

4. **Interceptor Adds Token to Request**:
```
GET http://productservice:8081/product/1
Authorization: Bearer eyJhbGci...
```

5. **ProductService Validates Token**:
- Verifies JWT signature using Auth0's public key
- Checks token expiration
- Validates audience
- Extracts claims (if needed)

## JWT Token Structure

### Access Token

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "key-id-123"
  },
  "payload": {
    "iss": "https://dev-5nw6367bfpr277ec.us.auth0.com/",
    "sub": "auth0|user-id-123",
    "aud": "http://springboot-microservices-api",
    "exp": 1759750871,
    "iat": 1759664471,
    "scope": "openid profile email offline_access",
    "email": "user@example.com",
    "email_verified": true
  },
  "signature": "..."
}
```

**Claims Explained**:
- `iss` (Issuer): Who created the token (Auth0)
- `sub` (Subject): Who the token is about (user ID)
- `aud` (Audience): Who the token is for (our API)
- `exp` (Expiration): When the token expires
- `iat` (Issued At): When the token was created
- `scope`: Permissions granted

### ID Token

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "iss": "https://dev-5nw6367bfpr277ec.us.auth0.com/",
    "sub": "auth0|user-id-123",
    "aud": "YOUR_CLIENT_ID",
    "exp": 1759750871,
    "iat": 1759664471,
    "nonce": "random-nonce",
    "email": "user@example.com",
    "name": "John Doe",
    "picture": "https://example.com/avatar.jpg"
  }
}
```

**Purpose**: Contains user profile information

## Implementation

### CloudGateway Configuration

**Location**: `CloudGateway/src/main/resources/application-dev.yml`

```yaml
spring:
  security:
    oauth2:
      # Resource Server (validates incoming tokens)
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
          audiences: ${AUTH0_AUDIENCE}

      # OAuth2 Client (for user login)
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

**Security Configuration**:

```java
@Configuration
@EnableWebFluxSecurity
public class OktaOAuth2WebSecurity {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints
                .pathMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/**"
                ).permitAll()
                // All other endpoints require authentication
                .anyExchange().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .oauth2Login(withDefaults())  // Enable OAuth2 login
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(withDefaults())  // Enable JWT validation
            )
            .build();
    }
}
```

### OrderService Configuration

**Location**: `OrderService/src/main/resources/application-dev.yaml`

```yaml
spring:
  security:
    oauth2:
      # Resource Server (validates incoming tokens)
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}

      # OAuth2 Client (for service-to-service calls)
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
```

**OAuth2 Client Manager**:

```java
@Bean
public OAuth2AuthorizedClientManager clientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository) {

    OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder
        .builder()
        .clientCredentials()
        .refreshToken()  // Enable automatic token refresh
        .build();

    DefaultOAuth2AuthorizedClientManager manager = new DefaultOAuth2AuthorizedClientManager(
        clientRegistrationRepository,
        oAuth2AuthorizedClientRepository
    );

    manager.setAuthorizedClientProvider(provider);

    return manager;
}
```

**RestTemplate Interceptor**:

```java
@Component
public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Autowired
    private OAuth2AuthorizedClientManager clientManager;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        // Request OAuth2 token
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
            .withClientRegistrationId("internal-client")
            .principal("internal")
            .build();

        OAuth2AuthorizedClient client = clientManager.authorize(authorizeRequest);

        if (client != null && client.getAccessToken() != null) {
            String token = client.getAccessToken().getTokenValue();
            request.getHeaders().add("Authorization", "Bearer " + token);
            log.debug("Added OAuth2 token to request");
        } else {
            log.error("Failed to obtain OAuth2 access token");
            throw new OAuth2AuthenticationException(
                new OAuth2Error("access_token_unavailable")
            );
        }

        return execution.execute(request, body);
    }
}
```

### ProductService Configuration

**Location**: `ProductService/src/main/resources/application-dev.yml`

```yaml
spring:
  security:
    oauth2:
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
```

**Security Configuration**:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(withDefaults())
            )
            .build();
    }
}
```

## Environment Variables

### Option B: Two Separate Applications (Recommended)

**`.env` file**:

```bash
# CloudGateway - Regular Web Application (user login)
AUTH0_CLIENT_ID=Id7HefCrHPMRUDZRwar3fTdoaXuhmV2D
AUTH0_CLIENT_SECRET=BEcG0GZO3m3bo51Mmbs55e5hAjYNnMDVRXsejwV3XYewl3GmlsRA6ZSg70FBsyAQ

# Backend Services - Machine-to-Machine (service-to-service)
AUTH0_M2M_CLIENT_ID=FkXb5HwPqfrGbeF5HOdZlhC0YuijIvq0
AUTH0_M2M_CLIENT_SECRET=0HzIgs79AZo6c8Vf38xrMVQFD1wVJeprPlANyJaatGshGRoglAvVXQW_vSyRlC0N

# Shared Configuration
AUTH0_AUDIENCE=http://springboot-microservices-api
AUTH0_ISSUER_URI=https://dev-5nw6367bfpr277ec.us.auth0.com/
```

### Which Service Uses Which Auth0 App?

| Service | Auth0 App Type | Environment Variables | Purpose |
|---------|----------------|----------------------|---------|
| **CloudGateway** | Regular Web Application | `AUTH0_CLIENT_ID`<br>`AUTH0_CLIENT_SECRET` | Frontend user login |
| **OrderService** | Machine-to-Machine | `AUTH0_M2M_CLIENT_ID`<br>`AUTH0_M2M_CLIENT_SECRET` | Service calls |
| **ProductService** | N/A (resource server only) | `AUTH0_ISSUER_URI` | Token validation |
| **PaymentService** | N/A (resource server only) | `AUTH0_ISSUER_URI` | Token validation |

## Testing Authentication

### 1. Test User Login

```bash
# Open in browser
http://localhost:9090/authenticate/login

# Expected: Redirect to Auth0 login
# After login: Returns JSON with tokens
```

### 2. Test API with Token

```bash
# Extract access token from login response
TOKEN="eyJhbGciOiJkaXIi..."

# Make authenticated request
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:9090/product/1
```

### 3. Test Service-to-Service Auth

```bash
# Check OrderService logs for token acquisition
docker-compose -f docker-compose.dev.yml logs orderservice | grep -i "oauth\|token"

# Should see:
# "Successfully added OAuth2 access token to request"
```

### 4. Test Token Validation

```bash
# Request without token (should fail)
curl http://localhost:9090/product/1
# Response: 401 Unauthorized

# Request with invalid token
curl -H "Authorization: Bearer invalid-token" \
  http://localhost:9090/product/1
# Response: 401 Unauthorized

# Request with valid token
curl -H "Authorization: Bearer $VALID_TOKEN" \
  http://localhost:9090/product/1
# Response: 200 OK with product data
```

## Benefits

✅ **Industry Standard**
- OAuth 2.0 and OIDC are widely adopted
- Works with any OAuth2-compliant provider
- Well-documented and tested

✅ **Stateless Authentication**
- JWT tokens are self-contained
- No session storage needed
- Easy to scale horizontally

✅ **Automatic Token Management**
- RestTemplate interceptor handles tokens
- Automatic refresh when expired
- No manual token management in business logic

✅ **Centralized User Management**
- Auth0 handles user database
- Multi-factor authentication
- Social login support
- Enterprise SSO integration

✅ **Fine-Grained Authorization**
- Scope-based permissions
- Role-based access control (RBAC)
- Custom claims in tokens

## Security Best Practices

✅ **Use HTTPS in Production**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
```

✅ **Validate Tokens on Every Service**
```yaml
# All services should validate tokens
spring:
  security:
    oauth2:
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
```

✅ **Use Refresh Tokens**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          auth0:
            scope: openid,profile,email,offline_access  # offline_access = refresh token
```

✅ **Short Token Expiration**
- Access tokens: 24 hours (configurable in Auth0)
- Refresh tokens: 30 days
- Force re-authentication for sensitive operations

✅ **Validate Audience**
```java
// Ensure token is for your API
@Value("${spring.security.oauth2.resource-server.jwt.audiences}")
private List<String> audiences;
```

✅ **Never Log Tokens**
```java
// ❌ Bad
log.info("Access token: {}", accessToken);

// ✅ Good
log.info("Successfully authenticated user: {}", userId);
```

## Troubleshooting

### 401 Unauthorized on All Requests

**Causes**:
1. Auth0 configuration incorrect
2. Token not being sent
3. Token expired

**Solutions**:
```bash
# Check Auth0 configuration
docker-compose exec cloudgateway env | grep AUTH0

# Decode JWT to check expiration
echo "YOUR_TOKEN" | cut -d. -f2 | base64 -d | jq

# Test token manually
curl -H "Authorization: Bearer $TOKEN" http://localhost:9090/actuator/health
```

### Service-to-Service Calls Failing

**Causes**:
1. M2M app not configured
2. Wrong client credentials
3. API not authorized

**Solutions**:
```bash
# Check OrderService environment
docker-compose exec orderservice env | grep AUTH0_M2M

# Check logs
docker-compose logs orderservice | grep -i "oauth\|token\|error"

# Verify Auth0 M2M app is authorized for API
# Auth0 Dashboard → M2M App → APIs tab
```

### Refresh Token Not Returned

**Causes**:
1. `offline_access` scope missing
2. Regular Web App doesn't support refresh tokens

**Solutions**:
```yaml
# Add offline_access scope
spring:
  security:
    oauth2:
      client:
        registration:
          auth0:
            scope: openid,profile,email,offline_access  # Added offline_access
```

## Related Documentation

- [JWT Tokens](jwt-tokens.md) - Deep dive into JWT structure
- [CORS Configuration](cors-configuration.md) - Frontend integration
- [Service-to-Service Auth](service-to-service-auth.md) - OAuth2 client details
- [API Gateway](../patterns/api-gateway.md) - Gateway authentication

## References

- [OAuth 2.0 Specification](https://oauth.net/2/)
- [OpenID Connect](https://openid.net/connect/)
- [Auth0 Documentation](https://auth0.com/docs)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [JWT.io](https://jwt.io/) - JWT debugger

---

**Last Updated**: October 5, 2025
