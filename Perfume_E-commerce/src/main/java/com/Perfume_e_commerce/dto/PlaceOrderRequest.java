package com.Perfume_e_commerce.dto;

import com.Perfume_e_commerce.models.user.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {
    @NotNull(message = "Shipping address is required")
    @Valid
    private Address shippingAddress;

    private String promoCode;

    private List<String> selectedProductIds;
}
