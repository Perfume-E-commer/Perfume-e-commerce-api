package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.product.Product;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, ObjectId> {
    List<Product> findByIsActiveTrue();

    Page<Product> findByIsActiveTrue(Pageable pageable);


    List<Product> findByCategoryAndIsActiveTrue(String category);
    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

    Optional<Product> findById(String id);
    Optional<Product> findByIdAndIsActiveTrue(ObjectId id);

    @Query("{ 'isActive': true, " +
            "'$and': [ " +
            "  { '$or': [ { 'name': { '$regex': ?0, '$options': 'i' } }, { 'brand': { '$regex': ?0, '$options': 'i' } } ] }, " +
            "  { 'category': { '$regex': ?1, '$options': 'i' } }, " +
            "  { 'price': { '$gte': ?2 } }, " +
            "  { 'price': { '$lte': ?3 } } " +
            "] }")
    Page<Product> searchProducts(String keyword, String category, double minPrice, double maxPrice, Pageable pageable);
}
