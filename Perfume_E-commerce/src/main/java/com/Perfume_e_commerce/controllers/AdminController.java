package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.dto.response.BillingResponse;
import com.Perfume_e_commerce.models.order.Order;
import com.Perfume_e_commerce.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestParam String status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String id,
            @RequestParam String status) {

        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @GetMapping("/billing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BillingResponse>> getBillingRecords() {
        return ResponseEntity.ok(orderService.getBillingRecords());
    }
}
