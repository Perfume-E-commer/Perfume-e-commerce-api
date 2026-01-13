package com.Perfume_e_commerce.models.order;

import com.Perfume_e_commerce.models.user.Address;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "orders")
@Data
public class Order {

    @Id
    private String id;

    private String userId;
    private String orderNumber;
    private String userEmail;
    private String promoCodeUsed;
    private double discountAmount;
    private List<OrderItem> items = new ArrayList<>();

    private Double subtotal;
    private Double shippingCost;

    private double totalAmount;
    private BigDecimal total;
    private String status;
    private Address shippingAddress;
    private String paymentStatus;

    private Date shippedDate;
    private Date deliveryDate;

    private java.time.LocalDate estimatedDelivery;
    private LocalDateTime createdAt = LocalDateTime.now();
}
