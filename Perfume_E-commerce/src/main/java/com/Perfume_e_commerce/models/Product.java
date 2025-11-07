package com.Perfume_e_commerce.models;

import jakarta.validation.constraints.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.util.Date;

public class Product {
    @Id
    private ObjectId id;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Name too long")
    private String name;

    @NotBlank
    @Size(max = 50)
    private String brand;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    @Indexed
    private int stock = 0;

    @NotBlank
    @Pattern(regexp = "^(MEN|WOMEN)$", message = "Invalid category")
    @Indexed
    private String category; // "MEN", "WOMEN"

    private String imageUrl;

    // ... we can add scentNotes later ...

    @Indexed
    private boolean isActive = true; // So admins can hide products

    @Indexed
    private boolean isFeatured = false; // For the landing page

    private Date createdAt = new Date();
}
