package com.Perfume_e_commerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {
    
    @Id
    private String id; // Store MongoDB ObjectId as String
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private BigDecimal totalPrice;
    
    @Column(nullable = false, length = 20)
    private String status; // "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"
    
    @Column(columnDefinition = "TEXT")
    private String shippingAddress;
    
    private String paymentMethod;
    
    private LocalDateTime orderDate;
    
    private LocalDateTime deliveryDate;
}
