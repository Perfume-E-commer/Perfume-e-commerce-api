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
    private Integer totalItems;
    private Double shippingCost;
    private Double total;
    private String paymentMethod;

    private Date placedDate;
    private Date shippedDate;
    private Date estimatedArrivalDate;

    private List<OrderItemDto> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemDto {
        private String name;
        private String brand;
        private String variant;
        private String imageUrl;
    }
}