# Dev Containers

## Overview

Develop inside Docker containers with VS Code or Cursor.

## Prerequisites

- VS Code or Cursor
- Dev Containers extension
- Docker Desktop

## Configuration

**Each service has**: `.devcontainer/devcontainer.json`

```json
{
  "name": "ProductService Dev Container",
  "dockerComposeFile": "../docker-compose.dev.yml",
  "service": "productservice",
  "workspaceFolder": "/app",
  
  "customizations": {
    "vscode": {
      "extensions": [
        "vscjava.vscode-java-pack",
        "vscjava.vscode-spring-boot-dashboard",
        "redhat.java",
        "vmware.vscode-boot-dev-pack"
      ],
      "settings": {
        "java.home": "/usr/local/openjdk-17"
      }
    }
  },
  
  "forwardPorts": [8081, 5005],
  "postCreateCommand": "mvn clean compile"
}
```

## Usage

### VS Code / Cursor

1. **Open** service folder (e.g., `ProductService/`)
2. **Command Palette** (Cmd/Ctrl + Shift + P)
3. **"Dev Containers: Reopen in Container"**
4. Container builds and opens
5. Develop with full IDE features

### Features

✅ **Consistent Environment** - Same setup for all developers
✅ **Pre-installed Extensions** - Java, Spring Boot tools
✅ **Auto Port Forwarding** - Access services on localhost
✅ **Integrated Terminal** - Run commands inside container
✅ **Hot Reload** - Changes reflected immediately

## Multi-Container Development

Open **root workspace** to work on all services:

**.devcontainer/devcontainer.json** (root):
```json
{
  "name": "Microservices Workspace",
  "dockerComposeFile": "../docker-compose.dev.yml",
  "workspaceFolder": "/workspace",
  "workspaceMount": "source=${localWorkspaceFolder},target=/workspace,type=bind"
}
```

## Docker-in-Docker

Access Docker from within dev container:

```json
{
  "features": {
    "ghcr.io/devcontainers/features/docker-in-docker:2": {}
  }
}
```

---

**Last Updated**: October 5, 2025
