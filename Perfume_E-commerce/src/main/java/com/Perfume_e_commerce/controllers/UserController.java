package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.request.UpdateProfileRequest;
import com.Perfume_e_commerce.models.user.Address;
import com.Perfume_e_commerce.models.user.CreditCard;
import com.Perfume_e_commerce.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

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

        return userRepository.findByEmail(email).map(user -> {
            // Update basic info
            if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
            if (request.getLastName() != null) user.setLastName(request.getLastName());
            if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
            if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());
            if (request.getImageUrl() != null) user.setImageUrl(request.getImageUrl());

            if (request.getStreet() != null || request.getZipCode() != null) {
                Address address = new Address();
                address.setStreet(request.getStreet());
                address.setCity(request.getCity());
                address.setZipCode(request.getZipCode());
                address.setType("HOME");

                List<Address> addressList = new ArrayList<>();
                addressList.add(address);
                user.setAddresses(addressList);
            }

            if (request.getCreditCards() != null) {
                request.getCreditCards().forEach(CreditCard::maskCardNumber);
                user.setCreditCards(request.getCreditCards());
            }

            userRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}
