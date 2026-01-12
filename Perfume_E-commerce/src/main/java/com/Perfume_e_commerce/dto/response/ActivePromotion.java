package com.Perfume_e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivePromotion {
    private String id;
    private String code;
    private double discountPercentage;
    private String validUntil;
}