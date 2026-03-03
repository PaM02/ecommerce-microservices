package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private ProductClient productClient;  // ⭐ Injection du Feign Client

    private List<Order> orders = new ArrayList<>();
    private Long nextId = 1L;

    public OrderService(ProductClient productClient) {
        this.productClient = productClient;
        // Quelques commandes par défaut
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

    public Order createOrder(Long productId, String productName, Integer quantity, Double price) {
        Double totalPrice = price * quantity;
        Order order = new Order(nextId++, productId, productName, quantity, totalPrice);
        orders.add(order);
        return order;
    }
}
