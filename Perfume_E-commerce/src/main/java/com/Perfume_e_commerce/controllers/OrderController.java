package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.OrderRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.PlaceOrderRequest;
import com.Perfume_e_commerce.dto.response.OrderResponse;
import com.Perfume_e_commerce.models.order.Order;
import com.Perfume_e_commerce.services.OrderService;
import com.Perfume_e_commerce.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .map(user -> user.getId().toString())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        String userId = getCurrentUserId();
        String userEmail = getCurrentUserEmail();

        Order order = orderService.placeOrder(
                userId,
                userEmail,
                request.getShippingAddress(),
                request.getPromoCode(),
                request.getSelectedItems()
        );
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER') or hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Order> orders = orderRepository.findByUserId(userDetails.getId());

        List<OrderResponse> responseList = orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size, search));
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemDto> itemDtos = order.getItems().stream().map(item -> new OrderResponse.OrderItemDto(
                item.getProductId(),
                item.getProductName(),
                item.getBrand(),
                item.getVariant(),
                item.getImageUrl(),
                item.getCategory(),
                item.getOccasion(),
                item.getPrice(),
                item.getQuantity()
        )).collect(Collectors.toList());

        // Map Address
        OrderResponse.AddressDto addressDto = null;
        if (order.getShippingAddress() != null) {
            addressDto = new OrderResponse.AddressDto(
                    order.getShippingAddress().getFullName(),
                    order.getShippingAddress().getHouseNumber(),
                    order.getShippingAddress().getStreet(),
                    order.getShippingAddress().getVillage(),
                    order.getShippingAddress().getCommunity(),
                    order.getShippingAddress().getDistrict(),
                    order.getShippingAddress().getCity(),
                    order.getShippingAddress().getPhoneNumber()
            );
        }

        double total = order.getTotalAmount();
        double subtotal = (order.getSubtotal() != null) ? order.getSubtotal() : 0.0;
        double shipping = (order.getShippingCost() != null) ? order.getShippingCost() : 0.0;

        Date createdDate = convertToDate(order.getCreatedAt());
        Date shpDate = convertToDate(order.getShippedDate());
        Date delDate = convertToDate(order.getDeliveryDate());

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                createdDate,
                order.getStatus(),
                subtotal,
                order.getItems().size(),
                shipping,
                total,
                order.getPaymentMethod() != null ? order.getPaymentMethod() : "Online Payment",
                createdDate,
                shpDate,
                delDate,
                addressDto,
                itemDtos
        );
    }

    private Date convertToDate(Object dateObj) {
        if (dateObj == null) return null;

        if (dateObj instanceof java.time.LocalDateTime) {
            return Date.from(((java.time.LocalDateTime) dateObj)
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
        }

        if (dateObj instanceof java.time.LocalDate) {
            return Date.from(((java.time.LocalDate) dateObj)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant());
        }

        if (dateObj instanceof Date) {
            return (Date) dateObj;
        }

        return null;
    }
}
