package com.ebelemgnegre.OrderService.model;

import com.ebelemgnegre.OrderService.entity.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {
    private long productId;
    private long quantity;
    private long totalAmount;
    private PaymentMode paymentMode;
}
