package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.ProductClient;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.Product;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductClient productClient;  // ⭐ Injection du Feign Client
    private final OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    // ⭐ NOUVELLE MÉTHODE : Créer une commande en récupérant le produit
    public Order createOrderFromProduct(Long productId, Integer quantity) {
        // Appel au product-service via Feign
        Product product = productClient.getProductById(productId);

        if (product == null) {
            throw new RuntimeException("Produit non trouvé !");
        }

        Double totalPrice = product.getPrice() * quantity;
        Order order = new Order();
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);
        return orderRepository.save(order);
    }
}