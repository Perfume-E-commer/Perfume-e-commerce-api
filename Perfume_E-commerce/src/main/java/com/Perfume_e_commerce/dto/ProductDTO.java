package com.Perfume_e_commerce.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String id;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Category is required")
    private String category;
    private String description;

    private String scent;
    private String occasion;
    private String gender;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private BigDecimal discountedPrice;
    private Integer stock;
    private Integer orders;
    private LocalDate createdDate;
    private Boolean isFeatured;
    private Boolean isOnSale;
    private String imageUrl;
    private String summary;
    private Boolean taxIncluded;

    private Boolean isActive;
    private Integer minStockLevel;
}