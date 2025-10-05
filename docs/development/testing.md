# Testing

## Overview

Comprehensive testing strategy for microservices.

## Test Types

### Unit Tests

Test individual components in isolation:

```java
@Test
void shouldCalculateTotalPrice() {
    Product product = new Product(1L, "Laptop", new BigDecimal("999.99"), 10);
    
    BigDecimal total = product.getPrice().multiply(new BigDecimal(2));
    
    assertThat(total).isEqualByComparingTo(new BigDecimal("1999.98"));
}
```

### Integration Tests

Test Spring components together:

```java
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void shouldGetProductById() throws Exception {
        Product product = new Product(1L, "Laptop", new BigDecimal("999.99"), 10);
        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/product/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Laptop"));
    }
}
```

### E2E Tests with WireMock

Mock external services:

```java
@Test
void shouldHandleExternalServiceFailure() {
    stubFor(get(urlEqualTo("/external-api/data"))
        .willReturn(aResponse()
            .withStatus(500)
            .withBody("Service unavailable")));

    assertThatThrownBy(() -> orderService.createOrder(request))
        .isInstanceOf(ServiceException.class);
}
```

## Test Configuration

**application-test.yml**:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProductControllerTest

# Run with coverage
mvn test jacoco:report

# Integration tests only
mvn verify -DskipUnitTests
```

## Testing OAuth2

```java
@Test
@WithMockUser(authorities = {"SCOPE_read"})
void shouldGetProductWithAuth() throws Exception {
    mockMvc.perform(get("/product/1"))
        .andExpect(status().isOk());
}
```

## Best Practices

✅ Test business logic, not frameworks
✅ Use meaningful test names
✅ One assertion per test (ideally)
✅ Mock external dependencies
✅ Use H2 for database tests
✅ Test both success and failure paths

---

**Last Updated**: October 5, 2025
