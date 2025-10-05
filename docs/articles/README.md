# Updated Medium Articles (2025)

This directory contains updated versions of the Medium article series for the Spring Boot Microservices project.

## Articles Overview

### Part 1: Spring Boot and Spring Cloud
**File:** [article-1-spring-boot-spring-cloud.md](article-1-spring-boot-spring-cloud.md)

**Topics Covered:**
- Microservices architecture overview
- Service discovery with Netflix Eureka
- API Gateway with Spring Cloud Gateway
- Circuit breaker pattern with Resilience4j
- Service-to-service communication (OpenFeign + RestTemplate)
- Centralized configuration with Config Server

**Technology Stack:**
- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Java 17

---

### Part 2: OAuth2 Authentication with Auth0
**File:** [article-2-oauth2-auth0.md](article-2-oauth2-auth0.md)

**Major Updates from Original:**
- ❌ **Removed:** Okta integration
- ✅ **Added:** Auth0 setup and configuration
- ✅ **Updated:** OAuth2 flows and JWT validation
- ✅ **New:** Two Auth0 applications (Web + M2M)

**Topics Covered:**
- OAuth2 fundamentals
- Auth0 account and API setup
- Regular Web Application (Authorization Code flow)
- Machine-to-Machine Application (Client Credentials flow)
- JWT token validation at Gateway and Services
- Service-to-service authentication with automatic token injection
- CORS configuration for frontend applications
- Testing and troubleshooting

---

### Part 3: Docker and Kubernetes
**File:** [article-3-docker-kubernetes.md](article-3-docker-kubernetes.md)

**Topics Covered:**
- Docker containerization with multi-stage builds
- Docker Compose for local development
- Hot reload with volume mounts
- Production image optimization with Jib
- Kubernetes deployment (StatefulSet, Deployment, Service)
- ConfigMaps and Secrets management
- Horizontal Pod Autoscaler (HPA)
- CI/CD pipeline with GitHub Actions
- Production best practices (health checks, resource limits, monitoring)

**Updated Features:**
- Auth0 environment variables (not Okta)
- Development vs. Production Dockerfiles
- Remote debugging configuration
- Complete Kubernetes manifests

---

## Key Changes from Original Articles

### 1. OAuth2 Provider Migration
**Before (Original):**
- Okta as OAuth2 provider
- Single Okta application
- Okta-specific configuration

**After (Updated):**
- Auth0 as OAuth2 provider
- Two Auth0 applications:
  - Regular Web App (user login)
  - Machine-to-Machine (service-to-service)
- Auth0-specific configuration

**Environment Variables Changed:**
```bash
# OLD (Okta)
OKTA_CLIENT_ID
OKTA_CLIENT_SECRET
OKTA_ISSUER_URI

# NEW (Auth0)
AUTH0_CLIENT_ID
AUTH0_CLIENT_SECRET
AUTH0_M2M_CLIENT_ID
AUTH0_M2M_CLIENT_SECRET
AUTH0_ISSUER_URI
AUTH0_AUDIENCE
```

### 2. Technology Version Updates
- Spring Boot: 3.1.5 → 3.2.0
- Spring Cloud: 2022.0.4 → 2023.0.0
- Enhanced Resilience4j configuration
- Improved Swagger UI aggregation

### 3. Service Communication Enhancement
**Added:** Dual approach explanation
- OpenFeign for write operations (POST, PUT, DELETE)
- RestTemplate for read operations (GET)
- Rationale for using both approaches

### 4. Docker & Kubernetes Improvements
- Separate dev/prod Dockerfiles
- Hot reload setup for development
- Remote debugging ports (5005-5007)
- Auth0 secrets in Kubernetes
- Complete CI/CD pipeline example

---

## Usage Instructions

### For Medium Publication

1. **Copy Article Content:**
   - Copy from the .md files
   - Paste into Medium editor
   - Format code blocks with syntax highlighting

2. **Add Screenshots:**
   - Auth0 Dashboard screenshots
   - Eureka Dashboard
   - Swagger UI
   - Kubernetes Dashboard

3. **Update Links:**
   - Replace `[Your Repository Link]` with actual GitHub URL
   - Update author LinkedIn profile
   - Add canonical URL if republishing

4. **Update Metadata:**
   - Change publication date
   - Update tags/keywords
   - Add cover image

### For GitHub README

These articles can also be published as detailed guides in the main README or linked from the documentation index.

---

## Comparison with Original Articles

### What's Preserved
✅ Overall structure and flow
✅ Architecture diagrams (updated)
✅ Code examples (updated to Auth0)
✅ Teaching approach and explanations

### What's Updated
🔄 All Okta references → Auth0
🔄 OAuth2 configuration examples
🔄 Environment variable names
🔄 Security class names
🔄 Technology versions
🔄 Docker Compose configurations
🔄 Kubernetes manifests

### What's New
✨ Two Auth0 applications setup
✨ Service-to-service auth explanation
✨ RestTemplate OAuth2 interceptor details
✨ Development Dockerfiles
✨ Remote debugging setup
✨ Complete CI/CD pipeline
✨ Production best practices section

---

## Publishing Checklist

Before publishing to Medium:

- [ ] Replace all `[Your Repository Link]` placeholders
- [ ] Add actual Auth0 domain (or use example domain)
- [ ] Take screenshots for visual aids
- [ ] Test all code examples
- [ ] Verify all commands work
- [ ] Update author bio and date
- [ ] Add cover image (1200x630 recommended)
- [ ] SEO: Title, subtitle, tags
- [ ] Cross-link between parts (Part 1 → Part 2 → Part 3)

---

## Original vs Updated Articles Matrix

| Aspect | Original (Okta) | Updated (Auth0) | Status |
|--------|----------------|-----------------|--------|
| **Part 1** | Spring Boot 3.1.5 | Spring Boot 3.2.0 | ✅ Updated |
| **Part 2** | Okta OAuth2 | Auth0 OAuth2 | ✅ Rewritten |
| **Part 2** | Single app | Two apps (Web + M2M) | ✅ Enhanced |
| **Part 3** | Basic Docker | Dev + Prod Dockerfiles | ✅ Enhanced |
| **Part 3** | Basic K8s | Secrets + HPA + CI/CD | ✅ Enhanced |

---

## Contact

**Author:** Etienne Belem Gnegre
**GitHub:** [Your GitHub Profile]
**LinkedIn:** [Your LinkedIn Profile]
**Medium:** [@belemgnegreetienne](https://medium.com/@belemgnegreetienne)

---

**Last Updated:** January 2025
