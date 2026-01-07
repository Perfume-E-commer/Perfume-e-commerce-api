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
    List<Product> findByActiveTrue();

    Page<Product> findByActiveTrue(Pageable pageable);

    List<Product> findByCategoryAndActiveTrue(String category);

    Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

    Optional<Product> findById(String id);

    Optional<Product> findByIdAndActiveTrue(ObjectId id);

    @Query("{ 'active': true, " +
            "'$and': [ " +
            "  { '$or': [ { 'name': { '$regex': ?0, '$options': 'i' } }, { 'brand': { '$regex': ?0, '$options': 'i' } } ] }, " +
            "  { 'category': { '$regex': ?1, '$options': 'i' } }, " +
            "  { 'price': { '$gte': ?2 } }, " +
            "  { 'price': { '$lte': ?3 } } " +
            "] }")
    Page<Product> searchProducts(String keyword, String category, double minPrice, double maxPrice, Pageable pageable);
}
