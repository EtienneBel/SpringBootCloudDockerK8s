# Kubernetes

## Overview

Kubernetes orchestrates containerized applications at scale.

## Manifests Location

`/k8s/` directory contains all Kubernetes manifests.

## Resources

- **StatefulSet**: Eureka Server (persistent identity)
- **Deployment**: All microservices
- **Service**: Networking between pods
- **ConfigMap**: Application configuration
- **Secrets**: Sensitive data (create manually)

## Deploy to Kubernetes

```bash
# Apply all manifests
kubectl apply -f k8s/

# Check pod status
kubectl get pods

# Check services
kubectl get svc

# View logs
kubectl logs -f <pod-name>
```

## Scale Services

```bash
# Scale ProductService to 3 replicas
kubectl scale deployment productservice --replicas=3
```

---

**Last Updated**: October 5, 2025
