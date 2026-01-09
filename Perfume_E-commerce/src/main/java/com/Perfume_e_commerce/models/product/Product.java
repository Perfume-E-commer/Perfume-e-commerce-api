package com.Perfume_e_commerce.models.product;

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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    private String id;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Name too long")
    private String name;

    @NotBlank
    @Size(max = 50)
    private String brand;

    private String description;

    private String scent;

    private String occasion;

    private String summary;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal discountedPrice;

    @Min(value = 0, message = "Stock cannot be negative")
    @Indexed
    @Builder.Default
    private int stock = 0;

    @Builder.Default
    private int orders = 0;

    @NotBlank
    @Pattern(regexp = "^(MEN|WOMEN|UNISEX)$", message = "Invalid category. Must be MEN, WOMEN, or UNISEX")
    @Indexed
    private String category;

    private String imageUrl;

    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Builder.Default
    private boolean taxIncluded = false;

    @Builder.Default
    private boolean isOnSale = false;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private int totalReviews = 0;

    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();

    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    private ProductStory productStory;

    @Builder.Default
    private List<ProductFeature> features = new ArrayList<>();

    @Builder.Default
    private List<ScentNote> scentNotes = new ArrayList<>();

    @Builder.Default
    private int minStockLevel = 5;

    @Field("active")
    @Indexed
    @Builder.Default
    private boolean isActive = true;

    @Indexed
    @Builder.Default
    private boolean isFeatured = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();


    public void recalculateTotalStock() {
        if (this.variants != null && !this.variants.isEmpty()) {
            this.stock = this.variants.stream()
                    .mapToInt(ProductVariant::getStock)
                    .sum();
        }
    }

    public java.util.Optional<ProductVariant> getVariantBySize(String size) {
        if (this.variants == null) return java.util.Optional.empty();
        return this.variants.stream()
                .filter(v -> v.getSize().equalsIgnoreCase(size))
                .findFirst();
    }
}