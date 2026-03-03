package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")  // ⭐ Le nom dans Eureka !
public interface ProductClient {
    @GetMapping("/api/products/{id}")
    Product getProductById(@PathVariable Long id);
}
