package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.dto.response.BillingResponse;
import com.Perfume_e_commerce.dto.response.DashboardStatsResponse;
import com.Perfume_e_commerce.models.order.Order;
import com.Perfume_e_commerce.models.product.Product;
import com.Perfume_e_commerce.services.OrderService;
import com.Perfume_e_commerce.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Product>> getAllAdminProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(productService.getAllProductsForAdmin(page, size, search));
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size, search));
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(orderService.getDashboardStats());
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
