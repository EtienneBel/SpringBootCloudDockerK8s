package com.ebelemgnegre.PaymentService.service;

import com.ebelemgnegre.PaymentService.model.PaymentRequest;
import org.springframework.stereotype.Service;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);
}
