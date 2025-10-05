# Mastering Microservices with Spring Boot, Docker and Kubernetes (Part 3 - Updated 2025)

> Complete guide to containerizing and deploying Spring Boot microservices with Docker and Kubernetes, including CI/CD and production best practices.

---

## Introduction

In Parts 1 and 2, we built a secure microservices application with service discovery, API gateway, and OAuth2 authentication. Now we'll containerize it with Docker and deploy to Kubernetes.

**What You'll Learn:**
- ✅ Docker containerization with multi-stage builds
- ✅ Docker Compose for local development
- ✅ Hot reload with volume mounts
- ✅ Kubernetes deployment with ConfigMaps and Secrets
- ✅ Environment-specific configurations
- ✅ Production deployment strategies
- ✅ CI/CD pipeline with GitHub Actions

**Prerequisites:**
- Completed Parts 1 & 2
- Docker installed: https://docs.docker.com/get-docker/
- Kubernetes (minikube/kind/Docker Desktop): https://kubernetes.io/docs/setup/
- kubectl CLI: https://kubernetes.io/docs/tasks/tools/

**GitHub Repository:** [Your Repository Link]

---

## Table of Contents

1. [Why Docker and Kubernetes?](#why-docker-and-kubernetes)
2. [Docker Setup](#docker-setup)
3. [Docker Compose for Development](#docker-compose-for-development)
4. [Building Production Images](#building-production-images)
5. [Kubernetes Deployment](#kubernetes-deployment)
6. [Managing Secrets and ConfigMaps](#managing-secrets-and-configmaps)
7. [Scaling and Load Balancing](#scaling-and-load-balancing)
8. [CI/CD Pipeline](#cicd-pipeline)
9. [Monitoring and Logging](#monitoring-and-logging)
10. [Production Best Practices](#production-best-practices)

---

## Why Docker and Kubernetes?

### Docker Benefits

✅ **Consistency** - "Works on my machine" → Works everywhere
✅ **Isolation** - Each service runs in its own container
✅ **Lightweight** - Containers share OS kernel
✅ **Portability** - Run on any platform
✅ **Version Control** - Image tags for rollbacks

### Kubernetes Benefits

✅ **Auto-scaling** - Scale pods based on CPU/memory
✅ **Self-healing** - Restart failed containers
✅ **Load Balancing** - Distribute traffic across pods
✅ **Rolling Updates** - Zero-downtime deployments
✅ **Service Discovery** - Internal DNS for services
✅ **Configuration Management** - ConfigMaps and Secrets

---

## Docker Setup

### Project Structure

```
SpringBoot/
├── ServiceRegistry/
│   ├── Dockerfile.dev          # Development with hot reload
│   └── Dockerfile              # Production multi-stage build
├── ConfigServer/
│   ├── Dockerfile.dev
│   └── Dockerfile
├── CloudGateway/
│   ├── Dockerfile.dev
│   └── Dockerfile
├── ProductService/
│   ├── Dockerfile.dev
│   └── Dockerfile
├── OrderService/
│   ├── Dockerfile.dev
│   └── Dockerfile
├── PaymentService/
│   ├── Dockerfile.dev
│   └── Dockerfile
├── docker-compose.dev.yml      # Development environment
├── docker-compose.yml          # Production environment
└── .env.example                # Environment variables template
```

### Development Dockerfile

**Optimized for hot reload and fast iteration.**

**ServiceRegistry/Dockerfile.dev**
```dockerfile
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Expose port
EXPOSE 8761

# Run with Spring Boot DevTools for hot reload
CMD ["./mvnw", "spring-boot:run"]
```

**Key Features:**
- Layer caching for dependencies
- Source code mounted as volume (hot reload)
- Fast rebuild on code changes

### Production Dockerfile

**Multi-stage build for minimal image size.**

**ProductService/Dockerfile**
```dockerfile
# Stage 1: Build
FROM eclipse-temurin:17-jdk as builder

WORKDIR /app

# Copy Maven files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build JAR (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy only the JAR from build stage
COPY --from=builder /app/target/*.jar app.jar

# Create non-root user for security
RUN useradd -m appuser
USER appuser

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Benefits:**
- **Small image size** - JRE instead of JDK (~50% smaller)
- **Security** - Non-root user
- **Health checks** - Kubernetes integration
- **Fast startup** - Pre-built JAR

---

## Docker Compose for Development

### Full docker-compose.dev.yml

**docker-compose.dev.yml**
```yaml
version: "2.1"

# Development Docker Compose
# Optimized for local development with volume mounts for hot reload

services:
  # ======================================
  # Infrastructure Services
  # ======================================

  serviceregistry:
    build:
      context: ./ServiceRegistry
      dockerfile: Dockerfile.dev
    container_name: serviceregistry-dev
    ports:
      - '8761:8761'
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    volumes:
      - ./ServiceRegistry/src:/app/src
      - ./ServiceRegistry/target:/app/target
      - maven-repo:/root/.m2
    networks:
      - dev-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 10

  configserver:
    build:
      context: ./ConfigServer
      dockerfile: Dockerfile.dev
    container_name: configserver-dev
    ports:
      - '9296:9296'
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - EUREKA_SERVER_ADDRESS=http://serviceregistry:8761/eureka
    volumes:
      - ./ConfigServer/src:/app/src
      - ./ConfigServer/target:/app/target
      - maven-repo:/root/.m2
    networks:
      - dev-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9296/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 10
    depends_on:
      serviceregistry:
        condition: service_healthy

  cloudgateway:
    build:
      context: ./CloudGateway
      dockerfile: Dockerfile.dev
    container_name: cloudgateway-dev
    ports:
      - '9090:9090'
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - EUREKA_SERVER_ADDRESS=http://serviceregistry:8761/eureka
      - CONFIG_SERVER_URL=configserver
      - AUTH0_CLIENT_ID=${AUTH0_CLIENT_ID}
      - AUTH0_CLIENT_SECRET=${AUTH0_CLIENT_SECRET}
      - AUTH0_AUDIENCE=${AUTH0_AUDIENCE}
      - AUTH0_ISSUER_URI=${AUTH0_ISSUER_URI}
    volumes:
      - ./CloudGateway/src:/app/src
      - ./CloudGateway/target:/app/target
      - maven-repo:/root/.m2
    networks:
      - dev-network
    depends_on:
      configserver:
        condition: service_healthy

  # ======================================
  # Database Services
  # ======================================

  productDb:
    container_name: productDb-dev
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: productDb
    ports:
      - "3306:3306"
    volumes:
      - product_db_dev_data:/var/lib/mysql
    networks:
      - dev-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 10

  orderDb:
    container_name: orderDb-dev
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: orderDb
    ports:
      - "3307:3306"
    volumes:
      - order_db_dev_data:/var/lib/mysql
    networks:
      - dev-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 10

  paymentDb:
    container_name: paymentDb-dev
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: paymentDb
    ports:
      - "3308:3306"
    volumes:
      - payment_db_dev_data:/var/lib/mysql
    networks:
      - dev-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 10

  # ======================================
  # Business Services
  # ======================================

  product-service:
    build:
      context: ./ProductService
      dockerfile: Dockerfile.dev
    container_name: product-service-dev
    ports:
      - '8081:8081'
      - '5005:5005'  # Remote debugging
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - EUREKA_SERVER_ADDRESS=http://serviceregistry:8761/eureka
      - CONFIG_SERVER_URL=configserver
      - DB_HOST=productDb
      - AUTH0_ISSUER_URI=${AUTH0_ISSUER_URI}
      - AUTH0_AUDIENCE=${AUTH0_AUDIENCE}
      - JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
    volumes:
      - ./ProductService/src:/app/src
      - ./ProductService/target:/app/target
      - maven-repo:/root/.m2
    networks:
      - dev-network
    depends_on:
      productDb:
        condition: service_healthy
      configserver:
        condition: service_healthy

  order-service:
    build:
      context: ./OrderService
      dockerfile: Dockerfile.dev
    container_name: order-service-dev
    ports:
      - '8082:8082'
      - '5006:5006'  # Remote debugging
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - EUREKA_SERVER_ADDRESS=http://serviceregistry:8761/eureka
      - CONFIG_SERVER_URL=configserver
      - DB_HOST=orderDb
      - AUTH0_ISSUER_URI=${AUTH0_ISSUER_URI}
      - AUTH0_AUDIENCE=${AUTH0_AUDIENCE}
      - AUTH0_M2M_CLIENT_ID=${AUTH0_M2M_CLIENT_ID}
      - AUTH0_M2M_CLIENT_SECRET=${AUTH0_M2M_CLIENT_SECRET}
      - JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006
    volumes:
      - ./OrderService/src:/app/src
      - ./OrderService/target:/app/target
      - maven-repo:/root/.m2
    networks:
      - dev-network
    depends_on:
      orderDb:
        condition: service_healthy
      configserver:
        condition: service_healthy

  payment-service:
    build:
      context: ./PaymentService
      dockerfile: Dockerfile.dev
    container_name: payment-service-dev
    ports:
      - '8083:8083'
      - '5007:5007'  # Remote debugging
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - EUREKA_SERVER_ADDRESS=http://serviceregistry:8761/eureka
      - CONFIG_SERVER_URL=configserver
      - DB_HOST=paymentDb
      - AUTH0_ISSUER_URI=${AUTH0_ISSUER_URI}
      - AUTH0_AUDIENCE=${AUTH0_AUDIENCE}
      - JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5007
    volumes:
      - ./PaymentService/src:/app/src
      - ./PaymentService/target:/app/target
      - maven-repo:/root/.m2
    networks:
      - dev-network
    depends_on:
      paymentDb:
        condition: service_healthy
      configserver:
        condition: service_healthy

networks:
  dev-network:
    driver: bridge

volumes:
  maven-repo:
  product_db_dev_data:
  order_db_dev_data:
  payment_db_dev_data:
```

### Environment Variables Setup

**Create .env file:**
```bash
cp .env.example .env
```

**Edit .env:**
```properties
# Auth0 Configuration
AUTH0_CLIENT_ID=your_web_app_client_id
AUTH0_CLIENT_SECRET=your_web_app_client_secret
AUTH0_M2M_CLIENT_ID=your_m2m_client_id
AUTH0_M2M_CLIENT_SECRET=your_m2m_client_secret
AUTH0_AUDIENCE=http://springboot-microservices-api
AUTH0_ISSUER_URI=https://dev-xxxxxxx.us.auth0.com/
```

### Running with Docker Compose

**Start all services:**
```bash
docker-compose -f docker-compose.dev.yml up -d
```

**View logs:**
```bash
# All services
docker-compose -f docker-compose.dev.yml logs -f

# Specific service
docker-compose -f docker-compose.dev.yml logs -f product-service
```

**Check service status:**
```bash
docker-compose -f docker-compose.dev.yml ps
```

**Stop all services:**
```bash
docker-compose -f docker-compose.dev.yml down
```

**Rebuild after dependency changes:**
```bash
docker-compose -f docker-compose.dev.yml up -d --build
```

---

## Building Production Images

### Using Jib Maven Plugin (Recommended)

Jib builds optimized Docker images without a Docker daemon.

**Add to pom.xml:**
```xml
<plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>jib-maven-plugin</artifactId>
    <version>3.4.0</version>
    <configuration>
        <to>
            <image>etiennebel/${project.artifactId}:${project.version}</image>
        </to>
        <container>
            <jvmFlags>
                <jvmFlag>-Xms512m</jvmFlag>
                <jvmFlag>-Xmx1024m</jvmFlag>
            </jvmFlags>
            <ports>
                <port>8081</port>
            </ports>
            <format>OCI</format>
        </container>
    </configuration>
</plugin>
```

**Build and push:**
```bash
# Build all services
for service in ServiceRegistry ConfigServer CloudGateway ProductService OrderService PaymentService; do
  cd $service
  mvn compile jib:build
  cd ..
done
```

### Using Traditional Dockerfile

**Build production image:**
```bash
docker build -t etiennebel/productservice:1.0.0 -f ProductService/Dockerfile ProductService/
```

**Push to Docker Hub:**
```bash
docker push etiennebel/productservice:1.0.0
```

---

## Kubernetes Deployment

### Prerequisites

**Start Minikube:**
```bash
minikube start --cpus=4 --memory=8192
```

**Verify cluster:**
```bash
kubectl cluster-info
kubectl get nodes
```

### Kubernetes Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │           ConfigMaps & Secrets                   │   │
│  │  - eureka-cm (Eureka URL)                       │   │
│  │  - config-cm (Config Server URL)                │   │
│  │  - auth0-secret (OAuth2 credentials)            │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │         Service Registry (StatefulSet)           │   │
│  │  Replicas: 1 | Port: 8761                       │   │
│  └─────────────────────────────────────────────────┘   │
│                         ↓                               │
│  ┌─────────────────────────────────────────────────┐   │
│  │         Config Server (Deployment)               │   │
│  │  Replicas: 2 | Port: 9296                       │   │
│  └─────────────────────────────────────────────────┘   │
│                         ↓                               │
│  ┌─────────────────────────────────────────────────┐   │
│  │         Cloud Gateway (Deployment)               │   │
│  │  Replicas: 3 | Port: 9090 | LoadBalancer        │   │
│  └─────────────────────────────────────────────────┘   │
│              ↓             ↓             ↓              │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐       │
│  │  Product   │  │   Order    │  │  Payment   │       │
│  │  Service   │  │  Service   │  │  Service   │       │
│  │  Rep: 3    │  │  Rep: 3    │  │  Rep: 2    │       │
│  │  Port:8081 │  │  Port:8082 │  │  Port:8083 │       │
│  └────────────┘  └────────────┘  └────────────┘       │
│       ↓               ↓               ↓                │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐       │
│  │  MySQL     │  │  MySQL     │  │  MySQL     │       │
│  │  (Product) │  │  (Order)   │  │  (Payment) │       │
│  └────────────┘  └────────────┘  └────────────┘       │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Managing Secrets and ConfigMaps

### ConfigMaps (Non-sensitive Configuration)

**k8s/config-maps.yml**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: eureka-cm
data:
  eureka_service_address: http://service-registry-svc:8761/eureka

---
apiVersion: v1
kind: ConfigMap
metadata:
  name: config-cm
data:
  config_url: http://config-server-svc:9296
```

**Apply:**
```bash
kubectl apply -f k8s/config-maps.yml
```

### Secrets (Sensitive Data)

**Create Auth0 secrets:**
```bash
kubectl create secret generic auth0-secret \
  --from-literal=client-id='your_web_app_client_id' \
  --from-literal=client-secret='your_web_app_client_secret' \
  --from-literal=m2m-client-id='your_m2m_client_id' \
  --from-literal=m2m-client-secret='your_m2m_client_secret' \
  --from-literal=issuer-uri='https://dev-xxxxxxx.us.auth0.com/' \
  --from-literal=audience='http://springboot-microservices-api'
```

**Verify:**
```bash
kubectl get secrets
kubectl describe secret auth0-secret
```

### Service Registry (StatefulSet)

**k8s/service-registry-statefulset.yml**
```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: service-registry
spec:
  serviceName: service-registry-svc
  replicas: 1
  selector:
    matchLabels:
      app: service-registry
  template:
    metadata:
      labels:
        app: service-registry
    spec:
      containers:
      - name: service-registry
        image: etiennebel/serviceregistry:latest
        ports:
        - containerPort: 8761
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"

---
apiVersion: v1
kind: Service
metadata:
  name: service-registry-svc
spec:
  selector:
    app: service-registry
  ports:
  - port: 8761
    targetPort: 8761
  type: ClusterIP
```

### Product Service Deployment

**k8s/product-service-deployment.yml**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: product-service
  template:
    metadata:
      labels:
        app: product-service
    spec:
      containers:
      - name: product-service
        image: etiennebel/productservice:latest
        ports:
        - containerPort: 8081
        env:
          - name: EUREKA_SERVER_ADDRESS
            valueFrom:
              configMapKeyRef:
                name: eureka-cm
                key: eureka_service_address
          - name: CONFIG_SERVER_URL
            valueFrom:
              configMapKeyRef:
                name: config-cm
                key: config_url
          - name: AUTH0_ISSUER_URI
            valueFrom:
              secretKeyRef:
                name: auth0-secret
                key: issuer-uri
          - name: AUTH0_AUDIENCE
            valueFrom:
              secretKeyRef:
                name: auth0-secret
                key: audience
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: product-service-svc
spec:
  selector:
    app: product-service
  ports:
  - port: 80
    targetPort: 8081
  type: ClusterIP
```

### Deploy All Services

```bash
# Apply in order
kubectl apply -f k8s/config-maps.yml
kubectl apply -f k8s/service-registry-statefulset.yml
kubectl apply -f k8s/config-server-deployment.yml
kubectl apply -f k8s/mysql-deployment.yml
kubectl apply -f k8s/product-service-deployment.yml
kubectl apply -f k8s/order-service-deployment.yml
kubectl apply -f k8s/payment-service-deployment.yml
kubectl apply -f k8s/cloud-gateway-deployment.yml
```

**Verify deployments:**
```bash
kubectl get all
kubectl get pods
kubectl get services
```

---

## Scaling and Load Balancing

### Manual Scaling

```bash
# Scale Product Service to 5 replicas
kubectl scale deployment product-service --replicas=5

# Verify
kubectl get pods -l app=product-service
```

### Horizontal Pod Autoscaler (HPA)

**Enable metrics server (Minikube):**
```bash
minikube addons enable metrics-server
```

**Create HPA:**
```bash
kubectl autoscale deployment product-service \
  --cpu-percent=70 \
  --min=2 \
  --max=10
```

**Check HPA status:**
```bash
kubectl get hpa
```

### Access via LoadBalancer

**Expose Cloud Gateway:**
```bash
kubectl expose deployment cloud-gateway \
  --type=LoadBalancer \
  --port=9090 \
  --target-port=9090 \
  --name=cloud-gateway-lb
```

**Get external IP (Minikube):**
```bash
minikube service cloud-gateway-lb --url
```

**Test:**
```bash
curl $(minikube service cloud-gateway-lb --url)/actuator/health
```

---

## CI/CD Pipeline

### GitHub Actions Workflow

**.github/workflows/deploy.yml**
```yaml
name: Build and Deploy

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2

    - name: Build with Maven
      run: |
        mvn clean install -DskipTests

    - name: Run tests
      run: |
        mvn test

    - name: Build Docker images with Jib
      env:
        DOCKER_USERNAME: ${{ secrets.DOCKER_USERNAME }}
        DOCKER_PASSWORD: ${{ secrets.DOCKER_PASSWORD }}
      run: |
        echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
        mvn compile jib:build

  deploy:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Configure kubectl
      uses: azure/setup-kubectl@v3

    - name: Set up Kubeconfig
      env:
        KUBE_CONFIG: ${{ secrets.KUBE_CONFIG }}
      run: |
        mkdir -p $HOME/.kube
        echo "$KUBE_CONFIG" > $HOME/.kube/config

    - name: Deploy to Kubernetes
      run: |
        kubectl apply -f k8s/
        kubectl rollout status deployment/product-service
        kubectl rollout status deployment/order-service
        kubectl rollout status deployment/payment-service
```

### GitHub Secrets Setup

1. Go to **Settings → Secrets → Actions**
2. Add these secrets:
   - `DOCKER_USERNAME` - Your Docker Hub username
   - `DOCKER_PASSWORD` - Your Docker Hub password
   - `KUBE_CONFIG` - Your Kubernetes config file

---

## Production Best Practices

### 1. Resource Limits

Always set resource requests and limits:

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

### 2. Health Checks

Configure liveness and readiness probes:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8081
  initialDelaySeconds: 60
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8081
  initialDelaySeconds: 30
  periodSeconds: 10
```

### 3. Security

✅ Run containers as non-root user
✅ Use secrets for sensitive data
✅ Enable RBAC (Role-Based Access Control)
✅ Use network policies
✅ Scan images for vulnerabilities

### 4. Logging

Use centralized logging:

```yaml
# Fluentd DaemonSet for log collection
kubectl apply -f https://raw.githubusercontent.com/fluent/fluentd-kubernetes-daemonset/master/fluentd-daemonset-elasticsearch.yaml
```

### 5. Monitoring

Deploy Prometheus and Grafana:

```bash
# Add Prometheus Helm repo
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install Prometheus
helm install prometheus prometheus-community/kube-prometheus-stack

# Access Grafana
kubectl port-forward svc/prometheus-grafana 3000:80
```

### 6. Backup Strategy

✅ Database backups (MySQL snapshots)
✅ ConfigMaps/Secrets backups
✅ PersistentVolume snapshots
✅ Regular disaster recovery drills

---

## Troubleshooting

### Issue 1: Pod CrashLoopBackOff

**Check logs:**
```bash
kubectl logs <pod-name>
kubectl describe pod <pod-name>
```

**Common causes:**
- Application crashes on startup
- Missing environment variables
- Database connection failure

### Issue 2: Service Not Reachable

**Check service:**
```bash
kubectl get svc
kubectl describe svc <service-name>
```

**Test from inside cluster:**
```bash
kubectl run test --image=busybox --rm -it -- wget -qO- http://product-service-svc
```

### Issue 3: ImagePullBackOff

**Cause:** Docker image not found

**Solution:**
```bash
# Check image name
kubectl describe pod <pod-name> | grep Image

# Verify image exists on Docker Hub
docker pull etiennebel/productservice:latest
```

---

## Conclusion

You've successfully:
- ✅ Containerized microservices with Docker
- ✅ Set up local development with Docker Compose
- ✅ Deployed to Kubernetes with ConfigMaps and Secrets
- ✅ Implemented auto-scaling and load balancing
- ✅ Created CI/CD pipeline with GitHub Actions
- ✅ Applied production best practices

Your microservices are now production-ready and cloud-native!

**Source Code:** [GitHub Repository Link]

**Questions?** Leave a comment or connect on LinkedIn!

---

**Author:** Etienne Belem Gnegre
**Date:** January 2025
**Tags:** #Docker #Kubernetes #Microservices #DevOps #SpringBoot #CI/CD

---

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Jib Maven Plugin](https://github.com/GoogleContainerTools/jib)
- [Helm Charts](https://helm.sh/)
- [Prometheus](https://prometheus.io/)
- [Grafana](https://grafana.com/)
