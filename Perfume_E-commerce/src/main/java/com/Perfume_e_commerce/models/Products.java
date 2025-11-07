package com.Perfume_e_commerce.models;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.util.Date;

public class Products {
    @Id
    private ObjectId id;

    private String name;
    private String brand;
    private String description;
    private BigDecimal price;

    @Indexed
    private int stock = 0;

    @Indexed
    private String category; // "MEN", "WOMEN", "UNISEX"

    private String imageUrl;

    // ... we can add scentNotes later ...

    @Indexed
    private boolean isActive = true; // So admins can hide products

    @Indexed
    private boolean isFeatured = false; // For the landing page

    private Date createdAt = new Date();
}
