package com.ebelemgnegre.OrderService.controller;

import com.ebelemgnegre.OrderService.model.OrderRequest;
import com.ebelemgnegre.OrderService.model.OrderResponse;
import com.ebelemgnegre.OrderService.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Log4j2
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> placeOrder(@RequestBody OrderRequest orderRequest){
        log.info("Placing Order");
        long orderId = orderService.placeOrder(orderRequest);
        log.info("Order placed successfully with orderId: {}", orderId);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(){
        return new ResponseEntity<>(orderService.getAllOrders(), HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable long orderId){
        return new ResponseEntity<>(orderService.getOrderDetails(orderId), HttpStatus.OK);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable long orderId,
            @RequestBody OrderRequest orderRequest){
        log.info("Updating order with id: {}", orderId);
        OrderResponse orderResponse = orderService.updateOrder(orderId, orderRequest);
        log.info("Order updated successfully");
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable long orderId){
        log.info("Deleting order with id: {}", orderId);
        orderService.deleteOrder(orderId);
        log.info("Order deleted successfully");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
