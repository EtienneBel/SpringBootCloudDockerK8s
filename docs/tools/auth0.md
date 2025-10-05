# Auth0

## Overview

Auth0 is a cloud-based authentication and authorization platform.

## Setup Steps

### 1. Create Account

Go to https://auth0.com/signup

### 2. Create API

- Name: SpringBoot Microservices API
- Identifier: `http://springboot-microservices-api`
- Signing Algorithm: RS256

### 3. Create Regular Web Application

- Name: SpringBoot Microservices Web
- Type: Regular Web Application
- Allowed Callback URLs: `http://localhost:9090/login/oauth2/code/auth0`
- Allowed Logout URLs: `http://localhost:9090`
- Grant Types: Authorization Code, Refresh Token

### 4. Create M2M Application

- Name: SpringBoot Microservices M2M
- Type: Machine to Machine
- Authorize: SpringBoot Microservices API
- Grant Types: Client Credentials

### 5. Configure Environment

```bash
# .env
AUTH0_CLIENT_ID=<web_app_client_id>
AUTH0_CLIENT_SECRET=<web_app_client_secret>
AUTH0_M2M_CLIENT_ID=<m2m_client_id>
AUTH0_M2M_CLIENT_SECRET=<m2m_client_secret>
AUTH0_AUDIENCE=http://springboot-microservices-api
AUTH0_ISSUER_URI=https://dev-xxx.us.auth0.com/
```

---

**Last Updated**: October 5, 2025
