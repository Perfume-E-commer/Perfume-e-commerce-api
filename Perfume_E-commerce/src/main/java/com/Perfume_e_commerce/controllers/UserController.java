package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.request.ChangePasswordRequest;
import com.Perfume_e_commerce.dto.request.UpdateProfileRequest;
import com.Perfume_e_commerce.models.user.Address;
import com.Perfume_e_commerce.models.user.CreditCard;
import com.Perfume_e_commerce.models.user.User;
import com.Perfume_e_commerce.services.UserDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User userUpdate = new User();
        userUpdate.setFirstName(request.getFirstName());
        userUpdate.setLastName(request.getLastName());
        userUpdate.setPhoneNumber(request.getPhoneNumber());
        userUpdate.setDateOfBirth(request.getDateOfBirth());
        userUpdate.setImageUrl(request.getImageUrl());

        return ResponseEntity.ok(userDetailsService.updateProfile(email, userUpdate));
    }

    @PostMapping("/address")
    public ResponseEntity<User> addAddress(@Valid @RequestBody Address address) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userDetailsService.addAddress(email, address));
    }

    @PostMapping("/card")
    public ResponseEntity<User> addCard(@RequestBody CreditCard card) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userDetailsService.addCreditCard(email, card));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userDetailsService.changePassword(email, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String imageUrl = userDetailsService.uploadAvatar(email, file);
        return ResponseEntity.ok(imageUrl);
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<User> deleteAddress(@PathVariable String addressId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userDetailsService.deleteAddress(email, addressId));
    }

    @DeleteMapping("/card/{cardId}")
    public ResponseEntity<User> deleteCard(@PathVariable String cardId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userDetailsService.deleteCreditCard(email, cardId));
    }
}
