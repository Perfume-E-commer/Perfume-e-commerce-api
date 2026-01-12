package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.models.product.Review;
import com.Perfume_e_commerce.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable String productId) {
        return ResponseEntity.ok(reviewService.getReviewsForProduct(productId));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addReview(@RequestBody Map<String, Object> request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName(); // Ensure this maps to ID
        String productId = (String) request.get("productId");
        Integer rating = (Integer) request.get("rating");
        String comment = (String) request.get("comment");

        try {
            Review review = reviewService.addReview(userId, productId, rating, comment);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
