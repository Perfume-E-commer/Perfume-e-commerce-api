package com.Perfume_e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LowStockItem {
    private String id;
    private String name;
    private int stock;
    private String imageUrl;
}
