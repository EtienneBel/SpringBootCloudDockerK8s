package com.ebelemgnegre.OrderService.service;

import com.ebelemgnegre.OrderService.model.OrderRequest;
import com.ebelemgnegre.OrderService.model.OrderResponse;

import java.util.List;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);
    OrderResponse getOrderDetails(long orderId);
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrder(long orderId, OrderRequest orderRequest);
    void deleteOrder(long orderId);
}
