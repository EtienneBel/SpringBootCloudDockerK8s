package com.ebelemgnegre.OrderService.controller;

import com.ebelemgnegre.OrderService.model.OrderRequest;
import com.ebelemgnegre.OrderService.model.OrderResponse;
import com.ebelemgnegre.OrderService.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/order")
@Log4j2
public class OrderController {

    @Autowired
    private OrderService orderService;

//    @PreAuthorize("hasAuthority('Customer')")
    @PostMapping("/placeOrder")
    public ResponseEntity<Long> placeOrder(@RequestBody OrderRequest orderRequest){
        log.info("Placing Order");
        long orderId = orderService.placeOrder(orderRequest);
        log.info("Order placed successfully with orderId: {}", orderId);
        return new ResponseEntity<>(orderId, HttpStatus.OK);
    }

//    @PreAuthorize("hasAuthority('Admin') || hasAuthority('Customer')")
    @GetMapping("{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable long orderId){
        return new ResponseEntity<>(orderService.getOrderDetails(orderId), HttpStatus.OK);
    }

//    @PreAuthorize("hasAuthority('Admin') || hasAuthority('OIDC_USER')")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(){
        return new ResponseEntity<>(orderService.getAllOrders(),
                HttpStatus.OK);
    }
}
