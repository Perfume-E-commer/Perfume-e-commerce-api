package com.Perfume_e_commerce.models.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariant {
    private String size;
    private BigDecimal price;
    private String imageUrl;
    private int stock;
    private int minStock = 5;
}
