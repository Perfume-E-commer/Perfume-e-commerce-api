package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.models.product.Product;
import com.Perfume_e_commerce.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    private String getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(user -> user.getId().toString())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/record/{productId}")
    public ResponseEntity<?> recordView(@PathVariable String productId) {
        try {
            String userId = getCurrentUserId();
            recommendationService.recordView(userId, productId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Product>> getRecommendations() {
        try {
            String userId = getCurrentUserId();
            return ResponseEntity.ok(recommendationService.getRecommendations(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
