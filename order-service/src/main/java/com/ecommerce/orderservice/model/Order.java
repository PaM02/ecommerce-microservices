package com.ecommerce.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class Order {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double totalPrice;
}
