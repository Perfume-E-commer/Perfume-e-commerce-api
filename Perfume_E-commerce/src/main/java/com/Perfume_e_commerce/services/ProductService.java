package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.models.Product;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
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

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(new ObjectId(id));
    }

    public Product updateProduct(String id, Product updatedDetails) {
        ObjectId objectId = new ObjectId(id);
        return productRepository.findById(objectId)
                .map(existingProduct -> {
                    existingProduct.setName(updatedDetails.getName());
                    existingProduct.setBrand(updatedDetails.getBrand());
                    existingProduct.setDescription(updatedDetails.getDescription());
                    existingProduct.setPrice(updatedDetails.getPrice());
                    existingProduct.setStock(updatedDetails.getStock());
                    existingProduct.setCategory(updatedDetails.getCategory());
                    existingProduct.setImageUrl(updatedDetails.getImageUrl());
                    // We generally don't update 'createdAt' or 'id'
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    public Product patchProduct(String id, Map<String, Object> updates) {
        ObjectId objectId = new ObjectId(id);
        return productRepository.findById(objectId)
                .map(product -> {
                    updates.forEach((key, value) -> {
                        switch (key) {
                            case "name":
                                product.setName((String) value);
                                break;
                            case "brand":
                                product.setBrand((String) value);
                                break;
                            case "description":
                                product.setDescription((String) value);
                                break;
                            case "price":
                                // JSON numbers often come as Integer/Double, so we convert safely
                                product.setPrice(new java.math.BigDecimal(String.valueOf(value)));
                                break;
                            case "stock":
                                product.setStock((Integer) value);
                                break;
                            case "category":
                                product.setCategory((String) value);
                                break;
                            case "imageUrl":
                                product.setImageUrl((String) value);
                                break;
                            case "isActive":
                                product.setActive((Boolean) value);
                                break;
                            // We ignore 'id' or 'createdAt' to prevent hacking
                        }
                    });
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

}
