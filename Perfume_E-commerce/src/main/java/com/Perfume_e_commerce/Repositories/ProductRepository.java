package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.Product;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, ObjectId> {
    // Find all active products
    List<Product> findByIsActiveTrue();

    // Find all active products by category (for filtering)
    List<Product> findByCategoryAndIsActiveTrue(String category);

    Optional<Product> findById(String id);
    Optional<Product> findByIdAndIsActiveTrue(String id);
}
