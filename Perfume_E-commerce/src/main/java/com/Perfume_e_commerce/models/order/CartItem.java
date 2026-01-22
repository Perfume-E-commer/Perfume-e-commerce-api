package com.Perfume_e_commerce.models.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    private String productId;
    private String productName;
    private int quantity;
    private double price;
    private String size;
    private String imageUrl;
    private String category;

    public CartItem(String productId, String productName, int quantity, double price, String size, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.size = size;
        this.imageUrl = imageUrl;
    }
}
