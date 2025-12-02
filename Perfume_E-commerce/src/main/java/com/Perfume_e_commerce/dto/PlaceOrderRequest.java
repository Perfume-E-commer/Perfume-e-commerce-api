package com.Perfume_e_commerce.dto;

import com.Perfume_e_commerce.models.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotNull(message = "Shipping address is required")
    @Valid
    private Address shippingAddress;

    private String promoCode;
}
