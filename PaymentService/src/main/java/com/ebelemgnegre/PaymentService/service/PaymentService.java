package com.ebelemgnegre.PaymentService.service;

import com.ebelemgnegre.PaymentService.model.PaymentRequest;
import com.ebelemgnegre.PaymentService.model.PaymentResponse;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);

    PaymentResponse getPaymentDetailsByOrderId(String orderId);
}
