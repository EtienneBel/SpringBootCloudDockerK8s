package com.ebelemgnegre.CloudGateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/orderServiceFallback")
    public String orderServiceFallback(){
        return "OrderService is down";
    }

    @GetMapping("/paymentServiceFallback")
    public String paymentServiceFallback(){
        return "PaymentService is down";
    }

    @GetMapping("/productServiceFallback")
    public String productServiceFallback(){
        return "ProductService is down";
    }
}
