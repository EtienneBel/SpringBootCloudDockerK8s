# CI/CD Pipeline

## Overview

Automated Continuous Integration and Continuous Deployment pipeline.

## GitHub Actions Example

**.github/workflows/ci-cd.yml**:

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build with Maven
      run: mvn clean package -DskipTests
    
    - name: Run Tests
      run: mvn test
    
    - name: Build Docker Images
      run: |
        docker-compose build
    
    - name: Push to Docker Hub
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker-compose push

  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Deploy to Kubernetes
      uses: azure/k8s-deploy@v1
      with:
        manifests: |
          k8s/service-registry-statefulset.yml
          k8s/config-server-deployment.yml
          k8s/cloud-gateway-deployment.yml
          k8s/product-service-deployment.yml
          k8s/order-service-deployment.yml
          k8s/payment-service-deployment.yml
        images: |
          your-registry/serviceregistry:latest
          your-registry/productservice:latest
        kubectl-version: 'latest'
```

## Pipeline Stages

1. **Build** - Compile code, run tests
2. **Package** - Create JAR files
3. **Containerize** - Build Docker images
4. **Push** - Push to registry
5. **Deploy** - Deploy to Kubernetes

## Secrets Configuration

Required secrets in GitHub/GitLab:

- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`
- `KUBE_CONFIG`
- `AUTH0_CLIENT_ID`
- `AUTH0_CLIENT_SECRET`

## Testing in Pipeline

```yaml
- name: Integration Tests
  run: |
    docker-compose -f docker-compose.test.yml up -d
    mvn verify
    docker-compose -f docker-compose.test.yml down
```

## Deployment Strategies

### Rolling Update (Default)

```yaml
spec:
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
```

### Blue-Green Deployment

```bash
# Deploy new version (green)
kubectl apply -f k8s/productservice-green.yml

# Switch traffic
kubectl patch service productservice -p '{"spec":{"selector":{"version":"green"}}}'

# Remove old version (blue)
kubectl delete -f k8s/productservice-blue.yml
```

---

**Last Updated**: October 5, 2025
