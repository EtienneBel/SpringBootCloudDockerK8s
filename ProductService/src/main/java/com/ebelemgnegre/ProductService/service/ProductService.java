package com.ebelemgnegre.ProductService.service;

import com.ebelemgnegre.ProductService.model.ProductRequest;
import com.ebelemgnegre.ProductService.model.ProductResponse;

public interface ProductService {
    long addProduct(ProductRequest productRequest);
    ProductResponse getProductById(long productId);

    void reduceQuantity(long productId, long quantity);
}
