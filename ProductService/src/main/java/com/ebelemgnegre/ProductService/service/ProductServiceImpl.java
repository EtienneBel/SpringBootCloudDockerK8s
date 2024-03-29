package com.ebelemgnegre.ProductService.service;

import com.ebelemgnegre.ProductService.entity.Product;
import com.ebelemgnegre.ProductService.exception.ProductServiceCustomException;
import com.ebelemgnegre.ProductService.model.ProductRequest;
import com.ebelemgnegre.ProductService.model.ProductResponse;
import com.ebelemgnegre.ProductService.repository.ProductRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.springframework.beans.BeanUtils.*;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("Adding product: {}", productRequest);

        Product product = Product.builder()
                .productName(productRequest.getProductName())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .build();

        productRepository.save(product);

        log.info("Product added: {}", product);
        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(long productId) {
        log.info("Getting product by id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException("Product not found", "404"));

        ProductResponse productResponse = new ProductResponse();
        copyProperties(product, productResponse);

        log.info("Product found: {}", productResponse);
        return productResponse;
    }

    public void reduceQuantity(long productId, long quantity) {
        log.info("Reduce quantity {} for Id: {}", quantity, productId);

        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductServiceCustomException(
                "Product with given Id not found",
                "PRODUCT_NOT_FOUND"
        ));

        if (product.getQuantity() < quantity) {
            throw new ProductServiceCustomException(
                    "Product does not have sufficient quantity",
                    "INSUFFICIENT_QUANTITY"
            );
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        log.info("Product Quantity updated Successfully");
    }
}
