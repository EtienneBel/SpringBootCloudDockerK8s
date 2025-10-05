# MySQL

## Overview

MySQL 8.0 is used as the production database for all microservices.

## Database per Service

| Service | Database | Port |
|---------|----------|------|
| ProductService | productDb | 3306 |
| OrderService | orderDb | 3307 |
| PaymentService | paymentDb | 3308 |

## Docker Configuration

```yaml
productDb:
  image: mysql:8.0
  environment:
    MYSQL_ROOT_PASSWORD: root
    MYSQL_DATABASE: productDb
  ports:
    - "3306:3306"
  volumes:
    - product_db_data:/var/lib/mysql
```

## Connection String

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3306/productDb
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## JPA Configuration

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
    show-sql: true
```

---

**Last Updated**: October 5, 2025
