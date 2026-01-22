package com.Perfume_e_commerce.dto;

import com.Perfume_e_commerce.models.user.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class PlaceOrderRequest {
    @NotNull(message = "Shipping address is required")
    @Valid
    private Address shippingAddress;

    private String promoCode;

    private String paymentMethod;

    private List<OrderItemRequest> selectedItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private String productId;
        private String size;
    }
}
