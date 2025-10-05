# CORS Configuration

## Overview

Cross-Origin Resource Sharing (CORS) allows frontend applications from different origins to access the API.

## Implementation

**CloudGateway/config/CorsConfig.java**:

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // React
            "http://localhost:4200",  // Angular
            "http://localhost:5173"   // Vite
        ));

        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
```

## Allowed Origins

Never use `*` with credentials:

```java
// ❌ Bad (security risk with credentials)
config.setAllowedOrigins(Arrays.asList("*"));
config.setAllowCredentials(true);

// ✅ Good (specific origins)
config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
config.setAllowCredentials(true);
```

## Frontend Usage

```javascript
// Frontend automatically sends cookies
fetch('http://localhost:9090/api/products', {
  credentials: 'include',
  headers: {
    'Authorization': `Bearer ${token}`
  }
})
```

## Best Practices

✅ Specific allowed origins (no wildcard)
✅ Enable credentials for cookies
✅ Set appropriate max age
✅ Limit allowed methods
✅ Update origins for production

---

**Last Updated**: October 5, 2025
