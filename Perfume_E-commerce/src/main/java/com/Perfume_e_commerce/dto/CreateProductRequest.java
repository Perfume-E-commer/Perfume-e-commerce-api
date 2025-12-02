package com.Perfume_e_commerce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Brand is required")
    private String brand;

    private String description;

    private String scent;
    private String occasion;
    private String gender;
    private String summary;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal discountedPrice;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^(MEN|WOMEN)$", message = "Category must be MEN or WOMEN")
    private String category;

    private String imageUrl;
    private Boolean taxIncluded;
    private Boolean isOnSale;
    private Boolean isFeatured;
}
