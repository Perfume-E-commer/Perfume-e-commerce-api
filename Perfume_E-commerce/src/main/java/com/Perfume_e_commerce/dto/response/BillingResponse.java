package com.Perfume_e_commerce.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingResponse {
    private String orderId;
    private String orderNumber;
    private String customerEmail;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private LocalDateTime date;
}
