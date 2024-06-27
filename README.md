
# Spring Boot Cloud Docker Kubernetes Project

This repository demonstrates a microservices architecture using Spring Boot, Docker, and Kubernetes (Minikube). The project includes multiple microservices, a configuration server, a service registry, and a cloud gateway and MySQL as database. The project illustrates how to containerize and orchestrate these services using Docker and Kubernetes.

## Prerequisites

- JDK 17
- Docker
- Minikube
- kubectl

## Project Structure

- **CloudGateway/**: Contains the Cloud Gateway microservice.
- **ConfigServer/**: Contains the Config Server microservice.
- **OrderService/**: Contains the Order Service microservice.
- **PaymentService/**: Contains the Payment Service microservice.
- **ProductService/**: Contains the Product Service microservice.
- **ServiceRegistry/**: Contains the Service Registry microservice.
- **k8s/**: Kubernetes manifests for deploying the microservices and MySQL databases.

## Getting Started

### Step 1: Clone the Repository

```sh
git clone https://github.com/EtienneBel/SpringBootCloudDockerK8s.git
cd SpringBootCloudDockerK8s
git checkout k8s
```

### Step 2: Build the Docker Images

Navigate to each microservice directory and build the Docker images:

```sh
cd CloudGateway
mvn clean install jib:build

cd ../ConfigServer
mvn clean install jib:build

cd ../OrderService
mvn clean install jib:build

cd ../PaymentService
mvn clean install jib:build

cd ../ProductService
mvn clean install jib:build.

cd ../ServiceRegistry
mvn clean install jib:build
```

### Step 3: Start Minikube

Set up a local Kubernetes cluster using Minikube. Then, once installed, ensure it is running by executing:

```sh
minikube start
```


### Step 5: Deploy Microservices

Apply the Kubernetes manifests for the microservices:

```sh
kubectl apply -f k8s
```

### Step 6: Verify Deployments

Check the status of your deployments and services:

```sh
kubectl get deployments
kubectl get services
```

### Step 7: Accessing the Minikube Dashboard

You can access the Kubernetes dashboard using Minikube for a visual overview of your cluster:

```sh
minikube dashboard
```

## Cleaning Up

To delete all resources created by this project, run:

```sh
kubectl delete -f k8s/
minikube stop
minikube delete
```
