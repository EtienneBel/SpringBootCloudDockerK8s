# JWT Tokens

## Overview

JSON Web Tokens (JWT) are self-contained tokens that carry claims about a user.

## JWT Structure

```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

[Header].[Payload].[Signature]
```

## Header

```json
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "key-id"
}
```

## Payload (Claims)

```json
{
  "iss": "https://dev-xxx.auth0.com/",
  "sub": "auth0|user-123",
  "aud": "http://springboot-microservices-api",
  "exp": 1759750871,
  "iat": 1759664471,
  "scope": "openid profile email",
  "email": "user@example.com"
}
```

## Validation

All services validate JWT:

```yaml
spring:
  security:
    oauth2:
      resource-server:
        jwt:
          issuer-uri: ${AUTH0_ISSUER_URI}
```

## Best Practices

✅ Short expiration (24 hours)
✅ Validate on every service
✅ Use refresh tokens
✅ Never log tokens
✅ Validate signature, issuer, audience

## Decode JWT (for debugging)

```bash
echo "YOUR_TOKEN" | cut -d. -f2 | base64 -d | jq
```

---

**Last Updated**: October 5, 2025
