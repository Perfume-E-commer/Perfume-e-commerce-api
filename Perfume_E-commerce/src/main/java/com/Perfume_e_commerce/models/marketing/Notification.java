package com.Perfume_e_commerce.models.marketing;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Data
public class Notification {
    @Id
    private String id;

    private String userId; // Who receives this? (Admin's ID)

    private String type; // "STOCK_ALERT", "ORDER_UPDATE", etc.

    private String message;

    private boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification(String userId, String type, String message) {
        this.userId = userId;
        this.type = type;
        this.message = message;
    }
}
