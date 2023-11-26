package com.ebelemgnegre.OrderService.service;

import com.ebelemgnegre.OrderService.model.OrderRequest;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);
}
