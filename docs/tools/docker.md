# Docker

## Overview

Docker provides containerization for consistent deployment across environments.

## Dockerfile Variants

### Production (Dockerfile.prod)

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Development (Dockerfile.dev)

```dockerfile
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app
COPY src ./src
COPY pom.xml .
ENTRYPOINT ["mvn", "spring-boot:run"]
```

## Build Image

```bash
# Build production image
docker build -f Dockerfile.prod -t productservice:latest .

# Using Jib (no Docker daemon needed)
mvn clean compile jib:dockerBuild
```

## Run Container

```bash
# Run single service
docker run -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=dev \
  productservice:latest

# Run with Docker Compose
docker-compose -f docker-compose.dev.yml up -d
```

## Volume Mounts (Development)

```yaml
volumes:
  - ./ProductService/src:/app/src        # Live code changes
  - ./ProductService/target:/app/target  # Compiled classes
  - maven-repo:/root/.m2                 # Maven cache
```

---

**Last Updated**: October 5, 2025
