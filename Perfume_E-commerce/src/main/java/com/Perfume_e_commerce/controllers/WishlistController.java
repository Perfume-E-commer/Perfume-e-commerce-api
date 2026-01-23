package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.request.WishlistRequest;
import com.Perfume_e_commerce.dto.response.WishlistResponse;
import com.Perfume_e_commerce.models.product.Product;
import com.Perfume_e_commerce.models.user.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WishlistController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getWishlist() {
        User user = getAuthenticatedUser();
        List<User.WishlistItem> items = user.getWishlist();

        if (items == null || items.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<ObjectId> objectIds = items.stream()
                .map(item -> new ObjectId(item.getProductId()))
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAllById(objectIds);

        List<WishlistResponse> response = new ArrayList<>();
        for (User.WishlistItem item : items) {
            Optional<Product> productOpt = products.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst();

            if (productOpt.isPresent()) {
                response.add(new WishlistResponse(productOpt.get(), item.getSize()));
            }
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<List<User.WishlistItem>> addToWishlist(@RequestBody WishlistRequest request) {
        User user = getAuthenticatedUser();

        if (user.getWishlist() == null) {
            user.setWishlist(new ArrayList<>());
        }

        boolean exists = user.getWishlist().stream().anyMatch(item ->
                item.getProductId().equals(request.getProductId()) &&
                        (item.getSize() == null ? request.getSize() == null : item.getSize().equals(request.getSize()))
        );

        if (!exists) {
            user.getWishlist().add(new User.WishlistItem(request.getProductId(), request.getSize()));
            userRepository.save(user);
        }

        return ResponseEntity.ok(user.getWishlist());
    }

    @PostMapping("/remove")
    public ResponseEntity<List<User.WishliatItem>> removeFromWishlist(@RequestBody WishlistRequest request) {
        User user = getAuthenticatedUser();

        if (user.getWishlist() != null) {
            user.getWishlist().removeIf(item ->
                    item.getProductId().equals(request.getProductId()) &&
                            (item.getSize() == null ? request.getSize() == null : item.getSize().equals(request.getSize()))
            );
            userRepository.save(user);
        }

        return ResponseEntity.ok(user.getWishlist());
    }
}
