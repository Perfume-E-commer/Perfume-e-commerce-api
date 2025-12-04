package com.Perfume_e_commerce.models.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    private String userId;
    private String userName;
    private int stars;
    private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();
}
