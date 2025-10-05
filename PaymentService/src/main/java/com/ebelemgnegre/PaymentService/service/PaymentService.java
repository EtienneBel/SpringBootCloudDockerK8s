package com.ebelemgnegre.PaymentService.service;

import com.ebelemgnegre.PaymentService.model.PaymentRequest;
import com.ebelemgnegre.PaymentService.model.PaymentResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface PaymentService {
    long doPayment(PaymentRequest paymentRequest);
    PaymentResponse getPaymentDetailsByOrderId(Long orderId);
    PaymentResponse getPaymentDetailsById(Long paymentId);
    List<PaymentResponse> getAllPayments();
    PaymentResponse updatePayment(Long paymentId, PaymentRequest paymentRequest);
    void deletePayment(Long paymentId);
}
