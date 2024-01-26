package com.ebelemgnegre.PaymentService.controller;

import com.ebelemgnegre.PaymentService.model.PaymentRequest;
import com.ebelemgnegre.PaymentService.service.PaymentService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@Log4j2
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Long> doPayment(@RequestBody PaymentRequest paymentRequest){
        log.info("PaymentRequest : {}", paymentRequest);
        return new ResponseEntity<>(paymentService.doPayment(paymentRequest),
                HttpStatus.OK);
    }
}
