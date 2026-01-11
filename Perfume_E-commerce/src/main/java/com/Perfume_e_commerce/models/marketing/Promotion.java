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

    private Double discountPercentage;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;
    private Integer usageLimit;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isActive() {
        return active;
    }
}
