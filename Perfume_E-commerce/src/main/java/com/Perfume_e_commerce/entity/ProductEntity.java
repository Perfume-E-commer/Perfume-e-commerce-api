package com.Perfume_e_commerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {
    
    @Id
    private String id; // Store MongoDB ObjectId as String
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 50)
    private String brand;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private int stock;
    
    @Column(nullable = false, length = 10)
    private String category; // "MEN", "WOMEN"
    
    @Column(length = 500)
    private String imageUrl;
    
    @Column(nullable = false)
    private int minStockLevel;
    
    @Column(nullable = false)
    private boolean isActive;
    
    @Column(nullable = false)
    private boolean isFeatured;
    
    private LocalDateTime createdAt;
}
