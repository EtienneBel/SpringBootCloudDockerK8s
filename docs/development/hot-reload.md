# Hot Reload Development

## Overview

Development setup with automatic application restart on code changes.

## Spring Boot DevTools

**Dependency**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**Features**:
- Automatic restart on classpath changes
- LiveReload support
- Property defaults for development

## Docker Volume Mounts

**docker-compose.dev.yml**:
```yaml
productservice:
  volumes:
    - ./ProductService/src:/app/src        # Source code
    - ./ProductService/target:/app/target  # Compiled classes
    - maven-repo:/root/.m2                 # Maven cache
```

## How It Works

1. Edit code locally
2. IDE compiles code to `target/`
3. Spring Boot DevTools detects change
4. Application restarts automatically (fast ~2-5 seconds)

## IDE Configuration

### IntelliJ IDEA

1. **Enable Build on Save**:
   - Settings → Build → Compiler → Build project automatically

2. **Allow auto-make in running app**:
   - Registry → `compiler.automake.allow.when.app.running`

### VS Code

1. **Java Extension Pack** installed
2. **Auto-save** enabled
3. DevTools detects changes automatically

## Maven Spring Boot Plugin

```bash
# Run with Maven (hot reload)
mvn spring-boot:run

# With JVM arguments
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug"
```

## Workflow

```bash
# Start services with hot reload
docker-compose -f docker-compose.dev.yml up -d

# Edit code in your IDE
# Changes automatically reflected in running container

# View logs to see restart
docker-compose -f docker-compose.dev.yml logs -f productservice
```

## Performance Tips

✅ Use DevTools (fast restart)
✅ Volume mount source code
✅ Shared Maven repository
✅ Exclude test resources from restart trigger

---

**Last Updated**: October 5, 2025
