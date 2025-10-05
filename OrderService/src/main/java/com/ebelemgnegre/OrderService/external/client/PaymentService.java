package com.ebelemgnegre.OrderService.external.client;

import com.ebelemgnegre.OrderService.external.request.PaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentService {
    @PostMapping("/api/payments")
    ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest);
}