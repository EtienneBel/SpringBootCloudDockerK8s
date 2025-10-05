package com.ebelemgnegre.ProductService.service;

import com.ebelemgnegre.ProductService.model.ProductRequest;
import com.ebelemgnegre.ProductService.model.ProductResponse;

import java.util.List;

public interface ProductService {
    long addProduct(ProductRequest productRequest);
    ProductResponse getProductById(long productId);
    List<ProductResponse> getAllProducts();
    ProductResponse updateProduct(long productId, ProductRequest productRequest);
    void deleteProduct(long productId);
    void reduceQuantity(long productId, long quantity);
}
