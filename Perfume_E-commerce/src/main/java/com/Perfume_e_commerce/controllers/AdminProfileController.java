package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.dto.request.UpdateProfileRequest;
import com.Perfume_e_commerce.models.user.User;
import com.Perfume_e_commerce.services.UserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/profile-api")
@RequiredArgsConstructor
public class AdminProfileController {

    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getAdminProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDetailsService.findByEmail(email).orElse(null);
        if (user == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateAdminProfile(@RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            UpdateProfileRequest request = objectMapper.readValue(data, UpdateProfileRequest.class);

            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            if (image != null && !image.isEmpty()) {
                String url = userDetailsService.uploadAvatar(email, image);
                request.setImageUrl(url);
            }

            User updated = userDetailsService.updateProfile(email, request);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
