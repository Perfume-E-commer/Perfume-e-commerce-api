package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.product.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProductIdOrderByCreatedAtDesc(String productId);

    boolean existsByUserIdAndProductId(String userId, String productId);
}
