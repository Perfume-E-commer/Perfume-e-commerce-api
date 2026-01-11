package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.dto.response.CustomerDetailResponse;
import com.Perfume_e_commerce.dto.response.CustomerListItemResponse;
import com.Perfume_e_commerce.dto.response.CustomerStatsResponse;
import com.Perfume_e_commerce.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final CustomerService customerService;

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CustomerListItemResponse>> searchCustomers(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(customerService.searchCustomersWithAnalytics(query, page, size));
    }

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

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportCustomers() {
        byte[] csvData = customerService.exportCustomersToCSV();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "customers.csv");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

}