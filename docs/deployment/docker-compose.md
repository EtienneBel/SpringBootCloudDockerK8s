# Docker Compose

## Overview

Docker Compose orchestrates multi-container applications for local development and testing.

## Files

- **docker-compose.yml** - Production stack
- **docker-compose.dev.yml** - Development stack with hot reload

## Development Stack

```bash
# Start all services
docker-compose -f docker-compose.dev.yml up -d

# View logs
docker-compose -f docker-compose.dev.yml logs -f

# Stop services
docker-compose -f docker-compose.dev.yml down

# Rebuild after dependency changes
docker-compose -f docker-compose.dev.yml up -d --build
```

## Service Dependencies

```yaml
cloudgateway:
  depends_on:
    configserver:
      condition: service_healthy
    
productservice:
  depends_on:
    productDb:
      condition: service_healthy
    configserver:
      condition: service_healthy
```

## Health Checks

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
  interval: 10s
  timeout: 5s
  retries: 10
```

## Volume Mounts (Hot Reload)

```yaml
volumes:
  - ./ProductService/src:/app/src        # Source code
  - ./ProductService/target:/app/target  # Compiled classes
  - maven-repo:/root/.m2                 # Maven cache
```

## Environment Variables

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=dev
  - EUREKA_SERVER_ADDRESS=http://serviceregistry:8761/eureka
  - AUTH0_CLIENT_ID=${AUTH0_CLIENT_ID}
```

## Networking

```yaml
networks:
  dev-network:
    driver: bridge
```

All services communicate via `dev-network`.

## Common Commands

```bash
# Check service status
docker-compose -f docker-compose.dev.yml ps

# Restart specific service
docker-compose -f docker-compose.dev.yml restart productservice

# View service logs
docker-compose -f docker-compose.dev.yml logs productservice

# Execute command in container
docker-compose -f docker-compose.dev.yml exec productservice sh

# Remove all containers and volumes
docker-compose -f docker-compose.dev.yml down -v
```

## Troubleshooting

**Services not starting**:
```bash
# Check logs
docker-compose -f docker-compose.dev.yml logs

# Rebuild images
docker-compose -f docker-compose.dev.yml build --no-cache
```

**Port conflicts**:
```bash
# Check what's using port
lsof -i :8081

# Change port in docker-compose.dev.yml
ports:
  - '8082:8081'  # Host:Container
```

---

**Last Updated**: October 5, 2025
