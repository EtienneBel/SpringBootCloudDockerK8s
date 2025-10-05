package com.ebelemgnegre.CloudGateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Circuit Breaker Fallbacks", description = "Fallback endpoints triggered when backend services are unavailable or experiencing issues")
public class FallbackController {

    @Operation(
            summary = "Order Service Fallback",
            description = "Fallback response when Order Service is down, experiencing high latency, or circuit breaker is open. " +
                    "Returns a friendly error message instead of failing the request."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Fallback message indicating Order Service is unavailable",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(implementation = String.class, example = "OrderService is down")
            )
    )
    @GetMapping("/orderServiceFallback")
    public String orderServiceFallback(){
        return "OrderService is down";
    }

    @Operation(
            summary = "Payment Service Fallback",
            description = "Fallback response when Payment Service is down, experiencing high latency, or circuit breaker is open. " +
                    "Returns a friendly error message instead of failing the request."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Fallback message indicating Payment Service is unavailable",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(implementation = String.class, example = "PaymentService is down")
            )
    )
    @GetMapping("/paymentServiceFallback")
    public String paymentServiceFallback(){
        return "PaymentService is down";
    }

    @Operation(
            summary = "Product Service Fallback",
            description = "Fallback response when Product Service is down, experiencing high latency, or circuit breaker is open. " +
                    "Returns a friendly error message instead of failing the request."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Fallback message indicating Product Service is unavailable",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(implementation = String.class, example = "ProductService is down")
            )
    )
    @GetMapping("/productServiceFallback")
    public String productServiceFallback(){
        return "ProductService is down";
    }
}
