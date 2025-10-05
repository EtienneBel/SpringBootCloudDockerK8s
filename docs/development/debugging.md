# Remote Debugging

## Overview

Debug Spring Boot applications running in Docker containers.

## Debug Ports

| Service | Debug Port |
|---------|-----------|
| ProductService | 5005 |
| OrderService | 5006 |
| PaymentService | 5007 |

## Docker Compose Configuration

```yaml
productservice:
  ports:
    - '8081:8081'
    - '5005:5005'  # Debug port
  environment:
    - JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
```

## Maven Debug Configuration

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## IDE Setup

### IntelliJ IDEA

1. **Run → Edit Configurations**
2. **+ → Remote JVM Debug**
3. **Host**: localhost
4. **Port**: 5005
5. **Module**: ProductService
6. **Apply & Debug**

### VS Code

**.vscode/launch.json**:
```json
{
  "type": "java",
  "name": "Debug ProductService",
  "request": "attach",
  "hostName": "localhost",
  "port": 5005
}
```

## Debugging Workflow

1. **Set breakpoint** in your IDE
2. **Start service** with debug enabled:
   ```bash
   docker-compose -f docker-compose.dev.yml up -d productservice
   ```
3. **Attach debugger** from IDE
4. **Trigger request** to hit breakpoint:
   ```bash
   curl http://localhost:9090/product/1
   ```

## Debug Logging

```yaml
logging:
  level:
    com.ebelemgnegre: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
```

## Troubleshooting

**Can't connect to debugger**:
```bash
# Check debug port is exposed
docker-compose -f docker-compose.dev.yml ps

# Check logs for debug agent
docker-compose logs productservice | grep -i jdwp
```

**Application freezes on startup**:
```bash
# Change suspend=y to suspend=n
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
```

---

**Last Updated**: October 5, 2025
