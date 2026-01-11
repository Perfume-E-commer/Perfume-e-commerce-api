package com.Perfume_e_commerce.models.product;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;

    private String userId;
    private String userName;   // Cached name so we don't look it up every time
    private String productId;

    private int rating;        // 1 to 5
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();
}
