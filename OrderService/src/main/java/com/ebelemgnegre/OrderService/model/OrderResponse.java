package com.ebelemgnegre.OrderService.model;

import com.ebelemgnegre.OrderService.entity.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private long orderId;
    private Instant orderDate;
    private String orderStatus;
    private long amount;
    private ProductDetails productDetails;
    private  PaymentDetails paymentDetails;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductDetails {
        private String productName;
        private long productId;
        private long quantity;
        private long price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentDetails {
        private long paymentId;
        private String paymentStatus;
        private PaymentMode paymentMode;
        private long amount;
        private Instant paymentDate;
        private long orderId;
    }
}
