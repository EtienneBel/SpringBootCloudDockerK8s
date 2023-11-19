package com.ebelemgnegre.ProductService.service;

import com.ebelemgnegre.ProductService.entity.Product;
import com.ebelemgnegre.ProductService.model.ProductRequest;
import com.ebelemgnegre.ProductService.repository.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("Adding product: {}", productRequest);

        Product product = Product.builder()
                .productName(productRequest.getName())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .build();

        productRepository.save(product);

        log.info("Product added: {}", product);
        return product.getProductId();
    }
}
