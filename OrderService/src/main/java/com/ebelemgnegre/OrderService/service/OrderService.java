package com.ebelemgnegre.OrderService.service;

import com.ebelemgnegre.OrderService.model.OrderRequest;
import com.ebelemgnegre.OrderService.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);
    OrderResponse getOrderDetails(long orderId);
}
