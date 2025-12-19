package com.Perfume_e_commerce.Repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.Perfume_e_commerce.models.product.Category;

public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByIsActiveTrue();
}
