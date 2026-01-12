package com.Perfume_e_commerce.models.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "inventory_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLog {
    @Id
    private String id;
    private String productId;
    private String changeType;
    private int quantityChange;
    private int newStockLevel;
    private LocalDateTime createdAt = LocalDateTime.now();
    public InventoryLog(String productId, String changeType, int quantityChange, int newStockLevel) {
        this.productId = productId;
        this.changeType = changeType;
        this.quantityChange = quantityChange;
        this.newStockLevel = newStockLevel;
        this.createdAt = LocalDateTime.now();
    }
}