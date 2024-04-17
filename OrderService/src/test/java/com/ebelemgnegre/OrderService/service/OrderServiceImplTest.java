package com.ebelemgnegre.OrderService.service;

import com.ebelemgnegre.OrderService.entity.Order;
import com.ebelemgnegre.OrderService.entity.PaymentMode;
import com.ebelemgnegre.OrderService.external.client.PaymentService;
import com.ebelemgnegre.OrderService.external.client.ProductService;
import com.ebelemgnegre.OrderService.external.response.PaymentResponse;
import com.ebelemgnegre.OrderService.external.response.ProductResponse;
import com.ebelemgnegre.OrderService.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    OrderService orderService = new OrderServiceImpl();

    @DisplayName("Get Order - Success Scenario")
    @Test
    void test_When_Order_Success(){
        //Mocking
        Order order = getMockOrder();
        when(orderRepository.findById((anyLong())))
                .thenReturn(Optional.of(order));
        when(restTemplate.getForObject("http://PRODUCT-SERVICE/product/" + order.getProductId(),
                ProductResponse.class)).thenReturn(getMockProductResponse());
        when(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/" + order.getId(),
                PaymentResponse.class)).thenReturn(getMockPaymentResponse());
        orderService.getOrderDetails(1);
     }

    private PaymentResponse getMockPaymentResponse() {
      return PaymentResponse.builder()
                .paymentId(1)
                .orderId(1)
                .amount(200)
                .paymentMode(PaymentMode.CASH)
                .paymentDate(Instant.now())
                .paymentDate(Instant.now())
                .status("ACCEPTED")
                .build();
    }

    private ProductResponse getMockProductResponse() {
        return ProductResponse.builder()
                .productId(2)
                .productName("iPhone 12 Pro Max")
                .price(100)
                .quantity(200)
                .build();
    }

    private Order getMockOrder() {
        return Order.builder()
                .id(1)
                .productId(2)
                .orderStatus("PLACED")
                .orderDate(Instant.now())
                .amount(100)
                .quantity(200)
                .build();
    }
}