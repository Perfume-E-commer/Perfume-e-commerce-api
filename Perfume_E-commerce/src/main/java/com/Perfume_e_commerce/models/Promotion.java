package com.Perfume_e_commerce.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Document(collection = "promotions")
@Data
public class Promotion {
    @Id
    private String id;

    @Indexed(unique = true)
    private String code; // e.g., "SUMMER10"

    private String description;

    private int discountPercent; // e.g., 10 for 10%

    private Date validUntil;

    private boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}
