package com.ebelemgnegre.ProductService.controller;

import com.ebelemgnegre.ProductService.model.ProductRequest;
import com.ebelemgnegre.ProductService.model.ProductResponse;
import com.ebelemgnegre.ProductService.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Long> addProduct(@RequestBody ProductRequest productRequest) {
        long productId = productService.addProduct(productRequest);
        return new ResponseEntity<>(productId, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") long productId){
        ProductResponse productResponse = productService.getProductById(productId);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") long productId,
            @RequestBody ProductRequest productRequest) {
        ProductResponse productResponse = productService.updateProduct(productId, productRequest);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") long productId) {
        productService.deleteProduct(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/reduceQuantity/{id}")
    ResponseEntity<Void> reduceQuantity(@PathVariable("id") long productId, @RequestParam long quantity){
        productService.reduceQuantity(productId, quantity);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
