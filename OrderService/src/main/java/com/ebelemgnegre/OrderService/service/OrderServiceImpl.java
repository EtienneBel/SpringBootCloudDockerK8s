package com.ebelemgnegre.OrderService.service;

import com.ebelemgnegre.OrderService.entity.Order;
import com.ebelemgnegre.OrderService.external.client.PaymentService;
import com.ebelemgnegre.OrderService.external.client.ProductService;
import com.ebelemgnegre.OrderService.external.request.PaymentRequest;
import com.ebelemgnegre.OrderService.model.OrderRequest;
import com.ebelemgnegre.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        log.info("Placing order Request: {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        Order order = Order.builder()
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .orderDate(Instant.now())
                .orderStatus("CREATED")
                .amount(orderRequest.getTotalAmount())
                .build();

        order = orderRepository.save(order);

        log.info("Calling Payment Service to complete the payment");

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(order.getId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getTotalAmount())
                .build();

        String orderStatus = null;
        try{
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully. Changing the order status to PLACED");
            orderStatus = "PLACED";
        } catch (Exception e){
            log.error("Error occurred in payment. Changing order status to PAYMENT_FAILED");
            orderStatus ="PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order placed successfully with orderId: {}", order.getId());

        return order.getId();
    }
}
