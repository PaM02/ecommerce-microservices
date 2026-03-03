package com.ecommerce.productservice.service;

import com.ecommerce.productservice.model.Product;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {

    // Liste simulée (en mémoire, pas de base de données pour l'instant)
    private List<Product> products = new ArrayList<>();

    public ProductService() {
        // Quelques produits par défaut
        products.add(new Product(1L, "Ordinateur", 999.99, 10));
        products.add(new Product(2L, "Souris", 29.99, 50));
        products.add(new Product(3L, "Clavier", 79.99, 30));
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public Product getProductById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
