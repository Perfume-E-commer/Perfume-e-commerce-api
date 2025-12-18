package com.Perfume_e_commerce.dto.request;

import com.Perfume_e_commerce.models.product.ProductFeature;
import com.Perfume_e_commerce.models.product.ProductStory;
import com.Perfume_e_commerce.models.product.ProductVariant;
import com.Perfume_e_commerce.models.product.ScentNote;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Brand is required")
    private String brand;

    private String description;

    private String summary;
    private String scent;
    private String occasion;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Discount price cannot be negative")
    private BigDecimal discountedPrice;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    @Min(value = 0)
    private int minStockLevel = 5;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^(MEN|WOMEN|UNISEX)$", message = "Category must be MEN or WOMEN")
    private String category;

    private List<ProductVariant> variants;
    private ProductStory productStory;
    private List<ProductFeature> features;
    private List<ScentNote> scentNotes;

    private String imageUrl;
    private List<String> images;

    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean isOnSale;
    private Boolean taxIncluded;
}
