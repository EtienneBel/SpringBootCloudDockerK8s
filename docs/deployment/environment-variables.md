# Environment Variables

## Overview

All sensitive configuration is externalized to environment variables.

## .env File

**Location**: `.env` (root directory)

```bash
# CloudGateway - Regular Web Application
AUTH0_CLIENT_ID=your_web_app_client_id
AUTH0_CLIENT_SECRET=your_web_app_client_secret

# Backend Services - Machine-to-Machine
AUTH0_M2M_CLIENT_ID=your_m2m_client_id
AUTH0_M2M_CLIENT_SECRET=your_m2m_client_secret

# Shared Configuration
AUTH0_AUDIENCE=http://springboot-microservices-api
AUTH0_ISSUER_URI=https://dev-xxx.us.auth0.com/
```

## Usage in Spring Boot

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          auth0:
            client-id: ${AUTH0_CLIENT_ID}
            client-secret: ${AUTH0_CLIENT_SECRET}
```

## Docker Compose

```yaml
cloudgateway:
  environment:
    - AUTH0_CLIENT_ID=${AUTH0_CLIENT_ID}
    - AUTH0_CLIENT_SECRET=${AUTH0_CLIENT_SECRET}
    - AUTH0_ISSUER_URI=${AUTH0_ISSUER_URI}
```

Docker Compose automatically loads `.env` file.

## Kubernetes

```bash
# Create secret from .env file
kubectl create secret generic app-secrets \
  --from-env-file=.env

# Use in deployment
env:
- name: AUTH0_CLIENT_ID
  valueFrom:
    secretKeyRef:
      name: app-secrets
      key: AUTH0_CLIENT_ID
```

## Verification

```bash
# Docker Compose
docker-compose -f docker-compose.dev.yml config | grep AUTH0

# Check container environment
docker-compose exec cloudgateway env | grep AUTH0

# Kubernetes
kubectl get secret app-secrets -o yaml
```

## Best Practices

✅ Never commit `.env` to git (in `.gitignore`)
✅ Use `.env.example` as template
✅ Different values per environment (dev/staging/prod)
✅ Use secrets management in production (Vault, AWS Secrets Manager)
✅ Rotate credentials regularly

---

**Last Updated**: October 5, 2025
