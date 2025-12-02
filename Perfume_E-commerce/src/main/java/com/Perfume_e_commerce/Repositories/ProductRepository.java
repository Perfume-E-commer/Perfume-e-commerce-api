package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.Product;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, ObjectId> {
    List<Product> findByIsActiveTrue();

    List<Product> findByCategoryAndIsActiveTrue(String category);

    List<Product> findByCategory(String category);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByIsFeaturedTrue();

    List<Product> findByIsOnSaleTrue();

    List<Product> findByStockLessThanEqual(int stock);

    @Query("{'name': {$regex: ?0, $options: 'i'}, 'category': ?1, 'gender': ?2}")
    List<Product> findWithFilters(String name, String category, String gender);

    Optional<Product> findById(String id);

    Optional<Product> findByIdAndIsActiveTrue(ObjectId id);
}