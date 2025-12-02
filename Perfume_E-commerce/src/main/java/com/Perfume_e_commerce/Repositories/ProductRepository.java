package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.product.Product;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, ObjectId> {
    List<Product> findByIsActiveTrue();

    List<Product> findByCategoryAndIsActiveTrue(String category);

    Optional<Product> findById(String id);
    Optional<Product> findByIdAndIsActiveTrue(ObjectId id);
}
