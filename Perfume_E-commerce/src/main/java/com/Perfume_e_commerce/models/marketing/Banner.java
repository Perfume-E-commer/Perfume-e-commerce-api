package com.Perfume_e_commerce.models.marketing;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "banners")
public class Banner {
    @Id
    private String id;

    private String title;           // e.g., "Summer Sale"
    private String subtitle;        // e.g., "Up to 50% off"
    private String imageUrl;        // URL to the image
    private String linkUrl;         // Where it clicks to (e.g., "/products?category=women")

    private boolean active;         // Is it currently showing?
    private int displayOrder;
}
