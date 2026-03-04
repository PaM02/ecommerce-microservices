package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.Product;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {

    @Autowired
    private ProductClient productClient;  // ⭐ Injection du Feign Client

    private final List<Order> orders = new ArrayList<>();
    private Long nextId = 1L;

    public OrderService() {
        orders.add(new Order(nextId++, 1L, "Ordinateur", 1, 999.99));
        orders.add(new Order(nextId++, 2L, "Souris", 2, 59.98));
    }

    public List<Order> getAllOrders() {
        return orders;
    }

    public Order getOrderById(Long id) {
        return orders.stream()
                .filter(o -> o.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // ⭐ NOUVELLE MÉTHODE : Créer une commande en récupérant le produit
    public Order createOrderFromProduct(Long productId, Integer quantity) {
        // Appel au product-service via Feign
        Product product = productClient.getProductById(productId);

        if (product == null) {
            throw new RuntimeException("Produit non trouvé !");
        }

        Double totalPrice = product.getPrice() * quantity;
        Order order = new Order(nextId++, productId, product.getName(), quantity, totalPrice);
        orders.add(order);
        return order;
    }
}