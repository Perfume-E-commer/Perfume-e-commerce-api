package com.Perfume_e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String id;
    private String orderId;
    private Date orderDate;
    private String status;

    private Double subtotal;
    private Double discountAmount;
    private String promoCode;
    private Integer totalItems;
    private Double shippingCost;
    private Double total;
    private String paymentMethod;
    private String customerEmail;

    private Date placedDate;
    private Date shippedDate;
    private Date estimatedArrivalDate;

    private AddressDto shippingAddress;

    private List<OrderItemDto> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemDto {
        private String productId;
        private String name;
        private String brand;
        private String variant;
        private String imageUrl;
        private String category;
        private String occasion;
        private Double price;
        private Integer quantity;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressDto {
        private String fullName;
        private String houseNumber;
        private String street;
        private String village;
        private String community;
        private String district;
        private String city;
        private String phoneNumber;
    }
}