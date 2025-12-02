package com.Perfume_e_commerce.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "carts")
@Data
public class Cart {
    @Id
    private String id;

    private String userId;

    private List<CartItem> items = new ArrayList<>();

    private double totalPrice = 0.0;

    public void calculateTotal() {
        this.totalPrice = this.items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
