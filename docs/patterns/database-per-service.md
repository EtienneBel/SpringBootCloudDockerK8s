# Database per Service Pattern

## Overview

Each microservice has its own private database that no other service can access directly.

## Architecture

```
ProductService → productDb (MySQL:3306)
OrderService → orderDb (MySQL:3307)
PaymentService → paymentDb (MySQL:3308)
```

## Benefits

✅ **Loose Coupling** - Services independent
✅ **Technology Diversity** - Different DB types per service
✅ **Independent Scaling** - Scale databases separately
✅ **Data Isolation** - No shared database contention

## Implementation

**Docker Compose**:
```yaml
productDb:
  image: mysql:8.0
  environment:
    MYSQL_DATABASE: productDb
  ports:
    - "3306:3306"

orderDb:
  image: mysql:8.0
  environment:
    MYSQL_DATABASE: orderDb
  ports:
    - "3307:3306"
```

## Best Practices

✅ One database per service
✅ No shared tables
✅ Use API calls for cross-service data
✅ Implement SAGA for distributed transactions

---

**Last Updated**: October 5, 2025
