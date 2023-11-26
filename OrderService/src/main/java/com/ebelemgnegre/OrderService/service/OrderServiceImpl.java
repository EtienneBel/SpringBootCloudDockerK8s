package com.ebelemgnegre.OrderService.service;

import com.ebelemgnegre.OrderService.model.OrderRequest;
import com.ebelemgnegre.OrderService.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public long placeOrder(OrderRequest orderRequest) {
        return 0;
    }
}
