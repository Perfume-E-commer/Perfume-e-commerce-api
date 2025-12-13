package com.Perfume_e_commerce.models.order;

import com.Perfume_e_commerce.models.user.Address;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
@Data
public class Order {

    @Id
    private String id;

    private String userId;

    private String orderNumber; // e.g., "ORD-123456"

    private String promoCodeUsed; // e.g., "SUMMER10"

    private double discountAmount;

    private List<OrderItem> items = new ArrayList<>();

    private double totalAmount;

    private String status; // "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"

    private Address shippingAddress;

    private String paymentStatus; // "PAID", "PENDING"

    private java.time.LocalDate estimatedDelivery;

    private LocalDateTime createdAt = LocalDateTime.now();
}
