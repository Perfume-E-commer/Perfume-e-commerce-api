package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.request.UpdateAdminProfileRequest;
import com.Perfume_e_commerce.dto.response.BillingResponse;
import com.Perfume_e_commerce.dto.response.DashboardStatsResponse;
import com.Perfume_e_commerce.dto.response.UserProfileResponse;
import com.Perfume_e_commerce.models.order.Order;
import com.Perfume_e_commerce.models.product.Product;
import com.Perfume_e_commerce.models.user.Address;
import com.Perfume_e_commerce.models.user.User;
import com.Perfume_e_commerce.services.FileStorageService;
import com.Perfume_e_commerce.services.OrderService;
import com.Perfume_e_commerce.services.ProductService;
import com.Perfume_e_commerce.services.UserDetailsImpl;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

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

    @PutMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> updateAdminProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateAdminProfileRequest request) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());

        userRepository.save(user);

        return ResponseEntity.ok(new UserProfileResponse(
                user.getId().toString(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                request.getAvatarUrl()
        ));
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAdminProfile(
            @RequestPart("data") UpdateAdminProfileRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        ObjectId userId = new ObjectId(userDetails.getId());
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        User admin = userOptional.get();

        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());

        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(image);
            admin.setImageUrl(imageUrl);
        }

        List<Address> addresses = admin.getAddresses();

        if (addresses == null) {
            addresses = new ArrayList<>();
        }

        Address addressToUpdate;

        if (addresses.isEmpty()) {
            addressToUpdate = new Address();
            addressToUpdate.setType("HOME");
            addresses.add(addressToUpdate);
        } else {
            addressToUpdate = addresses.get(0);
        }

        addressToUpdate.setStreet(request.getStreet());
        addressToUpdate.setCity(request.getCity());
        addressToUpdate.setZipCode(request.getZipCode());
        addressToUpdate.setFullName(request.getFirstName() + " " + request.getLastName());

        admin.setAddresses(addresses);

        User savedUser = userRepository.save(admin);

        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> getAdminProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        ObjectId userId = new ObjectId(userDetails.getId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        String street = "";
        String city = "";
        String zipCode = "";

        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            Address address = user.getAddresses().get(0); // Get the first address
            street = address.getStreet();
            city = address.getCity();
            zipCode = address.getZipCode();
        }

        UserProfileResponse response = new UserProfileResponse(
                user.getId().toHexString(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getImageUrl(),
                street,
                city,
                zipCode
        );

        return ResponseEntity.ok(response);
    }
}
