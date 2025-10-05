package com.ebelemgnegre.OrderService.service;

import com.ebelemgnegre.OrderService.entity.Order;
import com.ebelemgnegre.OrderService.exception.CustomException;
import com.ebelemgnegre.OrderService.external.client.PaymentService;
import com.ebelemgnegre.OrderService.external.client.ProductService;
import com.ebelemgnegre.OrderService.external.request.PaymentRequest;
import com.ebelemgnegre.OrderService.external.response.PaymentResponse;
import com.ebelemgnegre.OrderService.external.response.ProductResponse;
import com.ebelemgnegre.OrderService.model.OrderRequest;
import com.ebelemgnegre.OrderService.model.OrderResponse;
import com.ebelemgnegre.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;

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
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully. Changing the order status to PLACED");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error occurred in payment. Changing order status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order placed successfully with orderId: {}", order.getId());

        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new CustomException("Order not found for this id: " + orderId, "NOT_FOUND", 404));

        log.info("Invoking product service to fetch the product for id: {}", order.getProductId());

        ProductResponse productResponse = restTemplate.getForObject(
                "http://PRODUCT-SERVICE/product/" + order.getProductId(),
                ProductResponse.class
        );

        PaymentResponse paymentResponse = restTemplate.getForObject(
                "http://PAYMENT-SERVICE/payment/order/" + order.getId(),
                PaymentResponse.class
        );

        OrderResponse.ProductDetails productDetails = OrderResponse.ProductDetails.builder()
                .productName(productResponse.getProductName())
                .productId(productResponse.getProductId())
                .build();

        OrderResponse.PaymentDetails paymentDetails = OrderResponse.PaymentDetails.builder()
                .paymentId(paymentResponse.getPaymentId())
                .status(paymentResponse.getStatus())
                .paymentDate(paymentResponse.getPaymentDate())
                .paymentMode(paymentResponse.getPaymentMode())
                .build();


        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .amount(order.getAmount())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .build();

        return orderResponse;
    }

    @Override
    public List<OrderResponse> getAllOrders(){
        List<OrderResponse> orderResponses = new ArrayList<>();
        for (Order order: orderRepository.findAll()) {
            OrderResponse orderResponse = OrderResponse.builder()
                    .orderId(order.getId())
                    .orderStatus(order.getOrderStatus())
                    .orderDate(order.getOrderDate())
                    .amount(order.getAmount())
//                    .productDetails(productDetails)
//                    .paymentDetails(paymentDetails)
                    .build();
            orderResponses.add(orderResponse);
        }
        return orderResponses;
    }

    @Override
    public OrderResponse updateOrder(long orderId, OrderRequest orderRequest) {
        log.info("Updating order with id: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found for this id: " + orderId, "NOT_FOUND", 404));

        order.setProductId(orderRequest.getProductId());
        order.setQuantity(orderRequest.getQuantity());
        order.setAmount(orderRequest.getTotalAmount());
        order.setOrderStatus(orderRequest.getOrderStatus() != null ? orderRequest.getOrderStatus() : order.getOrderStatus());

        Order updatedOrder = orderRepository.save(order);

        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(updatedOrder.getId())
                .orderStatus(updatedOrder.getOrderStatus())
                .orderDate(updatedOrder.getOrderDate())
                .amount(updatedOrder.getAmount())
                .build();

        log.info("Order updated successfully: {}", orderResponse);
        return orderResponse;
    }

    @Override
    public void deleteOrder(long orderId) {
        log.info("Deleting order with id: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found for this id: " + orderId, "NOT_FOUND", 404));

        orderRepository.delete(order);
        log.info("Order deleted successfully");
    }
}
