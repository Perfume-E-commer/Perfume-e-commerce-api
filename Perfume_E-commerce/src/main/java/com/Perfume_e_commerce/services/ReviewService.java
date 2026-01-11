package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.OrderRepository;
import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.Repositories.ReviewRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.models.order.Order;
import com.Perfume_e_commerce.models.product.Product;
import com.Perfume_e_commerce.models.product.Review;
import com.Perfume_e_commerce.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Review> getReviewsForProduct(String productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public Review addReview(String userId, String productId, int rating, String comment) {
        // 1. Prevent duplicate reviews
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("You have already reviewed this product.");
        }

        // 2. Verified Purchase Check
        // Find orders by this user that contain this product ID
        List<Order> userOrders = orderRepository.findByUserId(userId);
        boolean hasPurchased = userOrders.stream()
                .anyMatch(order -> order.getItems().stream()
                        .anyMatch(item -> item.getProductId().equals(productId)));

        if (!hasPurchased) {
            throw new RuntimeException("Verified Purchase Required: You must buy this product before reviewing it.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Review review = new Review();
        review.setUserId(userId);
        review.setUserName(user.getFullName() != null ? user.getFullName() : user.getEmail());
        review.setProductId(productId);
        review.setRating(rating);
        review.setComment(comment);

        Review savedReview = reviewRepository.save(review);

        updateProductRating(productId);

        return savedReview;
    }

    private void updateProductRating(String productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        int count = reviews.size();

        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            if (product.getRating() == null) {
                product.setRating(new com.Perfume_e_commerce.models.product.Rating());
            }
            product.getRating().setAverageRating(average);
            product.getRating().setTotalReviews(count);
            productRepository.save(product);
        }
    }
}
