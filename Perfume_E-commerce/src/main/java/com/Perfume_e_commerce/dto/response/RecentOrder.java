package com.Perfume_e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentOrder {
    private String id;
    private String orderNumber;
    private String customerName;
    private double total;
    private String status;
    private String createdAt;
}
