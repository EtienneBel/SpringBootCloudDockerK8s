package com.ebelemgnegre.ProductService.repository;

import com.ebelemgnegre.ProductService.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
