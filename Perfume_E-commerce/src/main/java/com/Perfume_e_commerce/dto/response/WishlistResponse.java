package com.Perfume_e_commerce.dto.response;

import com.Perfume_e_commerce.models.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WishlistResponse {
    private Product product;
    private String selectedSize;
}
