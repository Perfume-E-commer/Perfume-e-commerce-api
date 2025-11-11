package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.models.Product;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> findByCategoryAndIsActiveTrue(String category){
        return productRepository.findByCategoryAndIsActiveTrue(category);
    }

    public Optional<Product> findById(String id){
        return productRepository.findById(id);
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    public Optional<Product> getProductById(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            return productRepository.findByIdAndIsActiveTrue(objectId);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
