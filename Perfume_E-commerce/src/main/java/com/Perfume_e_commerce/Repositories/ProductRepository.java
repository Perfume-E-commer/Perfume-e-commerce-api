package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.product.Product;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, ObjectId> {
    List<Product> findByIsActiveTrue();

    Page<Product> findByIsActiveTrue(Pageable pageable);


    List<Product> findByCategoryAndIsActiveTrue(String category);
    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

    Optional<Product> findById(String id);
    Optional<Product> findByIdAndIsActiveTrue(ObjectId id);
}
