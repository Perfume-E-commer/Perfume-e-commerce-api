package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.models.product.Product;
import com.Perfume_e_commerce.models.user.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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
    public ResponseEntity<List<Product>> getWishlist() {
        User user = getAuthenticatedUser();
        List<String> productIds = user.getWishlist();

        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        List<ObjectId> objectIds = productIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        List<Product> wishlistProducts = (List<Product>) productRepository.findAllById(objectIds);

        return ResponseEntity.ok(wishlistProducts);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<List<String>> addToWishlist(@PathVariable String productId) {
        User user = getAuthenticatedUser();

        if (user.getWishlist() == null) {
            user.setWishlist(new ArrayList<>());
        }

        // Add only if not already present
        if (!user.getWishlist().contains(productId)) {
            user.getWishlist().add(productId);
            userRepository.save(user);
        }

        return ResponseEntity.ok(user.getWishlist());
    }

    // 3. Remove from Wishlist
    @DeleteMapping("/{productId}")
    public ResponseEntity<List<String>> removeFromWishlist(@PathVariable String productId) {
        User user = getAuthenticatedUser();

        if (user.getWishlist() != null && user.getWishlist().contains(productId)) {
            user.getWishlist().remove(productId);
            userRepository.save(user);
        }

        return ResponseEntity.ok(user.getWishlist());
    }
}
