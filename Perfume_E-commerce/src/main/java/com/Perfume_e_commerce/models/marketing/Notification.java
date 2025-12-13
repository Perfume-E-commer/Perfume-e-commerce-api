package com.Perfume_e_commerce.models.marketing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Notification {
    @Id
    private String id;

    private ObjectId userId;

    private String type;

    private String message;

    private boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();

}
