# Kubernetes Deployment

## Overview

Deploy the microservices application to a Kubernetes cluster.

## Prerequisites

- Kubernetes cluster (Minikube, GKE, EKS, AKS)
- kubectl configured
- Docker images pushed to registry

## Build and Push Images

```bash
# Build with Jib and push to Docker Hub
mvn clean compile jib:build

# Or build and push manually
docker build -t your-registry/productservice:latest .
docker push your-registry/productservice:latest
```

## Deploy to Kubernetes

```bash
# Create namespace (optional)
kubectl create namespace microservices

# Apply all manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n microservices
kubectl get svc -n microservices
```

## Kubernetes Resources

### StatefulSet (Eureka)

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: serviceregistry
spec:
  serviceName: serviceregistry
  replicas: 1
  template:
    spec:
      containers:
      - name: serviceregistry
        image: your-registry/serviceregistry:latest
        ports:
        - containerPort: 8761
```

### Deployment (Microservices)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: productservice
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: productservice
        image: your-registry/productservice:latest
        ports:
        - containerPort: 8081
        env:
        - name: EUREKA_SERVER_ADDRESS
          value: "http://serviceregistry:8761/eureka"
```

### Service (Networking)

```yaml
apiVersion: v1
kind: Service
metadata:
  name: productservice
spec:
  type: ClusterIP
  ports:
  - port: 8081
    targetPort: 8081
  selector:
    app: productservice
```

## ConfigMaps and Secrets

```bash
# Create ConfigMap
kubectl create configmap app-config \
  --from-literal=SPRING_PROFILES_ACTIVE=prod

# Create Secret for Auth0
kubectl create secret generic auth0-credentials \
  --from-literal=CLIENT_ID=your_client_id \
  --from-literal=CLIENT_SECRET=your_client_secret
```

## Scaling

```bash
# Scale deployment
kubectl scale deployment productservice --replicas=5

# Autoscaling
kubectl autoscale deployment productservice \
  --min=2 --max=10 --cpu-percent=80
```

## Monitoring

```bash
# View logs
kubectl logs -f deployment/productservice

# Get pod details
kubectl describe pod <pod-name>

# Port forward for local access
kubectl port-forward svc/cloudgateway 9090:9090
```

## Ingress (Optional)

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: microservices-ingress
spec:
  rules:
  - host: api.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: cloudgateway
            port:
              number: 9090
```

---

**Last Updated**: October 5, 2025
