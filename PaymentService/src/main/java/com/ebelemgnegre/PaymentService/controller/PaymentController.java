package com.ebelemgnegre.PaymentService.controller;

import com.ebelemgnegre.PaymentService.model.PaymentRequest;
import com.ebelemgnegre.PaymentService.model.PaymentResponse;
import com.ebelemgnegre.PaymentService.service.PaymentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Log4j2
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest) {
        log.info("PaymentRequest : {}", paymentRequest);
        return new ResponseEntity<>(paymentService.doPayment(paymentRequest), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return new ResponseEntity<>(paymentService.getAllPayments(), HttpStatus.OK);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentDetailsById(@PathVariable Long paymentId) {
        return new ResponseEntity<>(paymentService.getPaymentDetailsById(paymentId), HttpStatus.OK);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentDetailsByOrderId(@PathVariable Long orderId) {
        return new ResponseEntity<>(paymentService.getPaymentDetailsByOrderId(orderId), HttpStatus.OK);
    }

    @PutMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> updatePayment(
            @PathVariable Long paymentId,
            @RequestBody PaymentRequest paymentRequest) {
        log.info("Updating payment with id: {}", paymentId);
        return new ResponseEntity<>(paymentService.updatePayment(paymentId, paymentRequest), HttpStatus.OK);
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long paymentId) {
        log.info("Deleting payment with id: {}", paymentId);
        paymentService.deletePayment(paymentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
