package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.Products;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository {
    // Find all active products
    List<Products> findByIsActiveTrue();

    // Find all active products by category (for filtering)
    List<Products> findByCategoryAndIsActiveTrue(String category);
}
