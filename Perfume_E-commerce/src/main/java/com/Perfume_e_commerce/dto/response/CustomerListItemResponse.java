package com.Perfume_e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerListItemResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean active;

    private String joinedAt;
    private long ordersCount;
    private double totalSpent;
    private String lastOrderDate;
}