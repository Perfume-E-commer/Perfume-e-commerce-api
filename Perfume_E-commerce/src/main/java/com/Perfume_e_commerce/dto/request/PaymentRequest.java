package com.Perfume_e_commerce.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long amount;
    private String currency;
    private String email;
}