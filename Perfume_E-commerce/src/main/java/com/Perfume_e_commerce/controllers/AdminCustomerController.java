package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.dto.response.CustomerDetailResponse;
import com.Perfume_e_commerce.dto.response.CustomerStatsResponse;
import com.Perfume_e_commerce.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final CustomerService customerService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerStatsResponse> getCustomerStats() {
        return ResponseEntity.ok(customerService.getCustomerStats());
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerDetailResponse> getCustomerDetails(@PathVariable String id) {
        return ResponseEntity.ok(customerService.getCustomerDetails(id));
    }

}