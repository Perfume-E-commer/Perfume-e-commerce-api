package com.Perfume_e_commerce.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private String scent;
    private String occasion;
    private String gender;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal discountedPrice;

    @Min(value = 0, message = "Stock cannot be negative")
    @Builder.Default
    @Indexed
    private int stock = 0;

    @Min(value = 0, message = "Orders cannot be negative")
    private Integer orders;

    @NotBlank
    @Pattern(regexp = "^(MEN|WOMEN)$", message = "Invalid category")
    @Indexed
    private String category; // "MEN", "WOMEN"

    private String imageUrl;

    // ... we can add scentNotes later ...
    private String summary;

    @Field("tax_included")
    @Builder.Default
    private Boolean taxIncluded = false;

    @Field("is_on_sale")
    @Builder.Default
    private Boolean isOnSale = false;

    @Builder.Default
    private int minStockLevel = 5;

    @Indexed
    @Builder.Default
    private boolean isActive = true;

    // @Indexed
    // private boolean isFeatured = false; // For the landing page

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDate createdDate = LocalDate.now();

    @Builder.Default
    @Indexed
    @Field("is_featured")
    private Boolean isFeatured = false;

}
