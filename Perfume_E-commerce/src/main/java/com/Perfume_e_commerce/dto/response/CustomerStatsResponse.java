package com.Perfume_e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerStatsResponse {
    private long totalCustomers;
    private double avgSpend;
    private long returningCustomers;
    private long newThisMonth;
}