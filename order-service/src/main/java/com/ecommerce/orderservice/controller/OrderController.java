package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {


    private final OrderService orderService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    // Endpoint simple pour créer une commande
    @PostMapping
    public Order createOrder(@RequestParam Long productId,
                             @RequestParam String productName,
                             @RequestParam Integer quantity,
                             @RequestParam Double price) {
        return orderService.createOrder(productId, productName, quantity, price);
    }
}