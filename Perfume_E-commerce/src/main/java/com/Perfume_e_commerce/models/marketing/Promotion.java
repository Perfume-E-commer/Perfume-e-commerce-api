package com.Perfume_e_commerce.models.marketing;

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
    private String code;

    private String description;

    private int discountPercent;

    private Date validUntil;

    private boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}
