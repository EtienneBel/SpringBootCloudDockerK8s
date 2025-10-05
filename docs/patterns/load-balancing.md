# Load Balancing Pattern

## Overview

**Pattern**: Client-Side Load Balancing
**Tool**: Spring Cloud LoadBalancer
**Integration**: Eureka Service Discovery

## How It Works

```
Gateway → Eureka → [Instance1, Instance2, Instance3]
                   ↓
            Round-robin selection
                   ↓
            Selected Instance
```

## Implementation

**Gateway Routes**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: PRODUCT-SERVICE
          uri: lb://PRODUCT-SERVICE  # Load balanced
```

## Load Balancing Strategies

- **Round Robin** (default)
- **Random**
- **Weighted Response Time**

## Benefits

✅ Distributes traffic evenly
✅ No single point of failure
✅ Automatic failover
✅ Better resource utilization

---

**Last Updated**: October 5, 2025
