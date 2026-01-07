package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.models.product.Product;
import com.Perfume_e_commerce.models.product.Rating;
import com.Perfume_e_commerce.models.user.User;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private UserRepository userRepository;

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
                    // --- Core Information ---
                    existingProduct.setName(updatedDetails.getName());
                    existingProduct.setBrand(updatedDetails.getBrand());
                    existingProduct.setDescription(updatedDetails.getDescription());
                    existingProduct.setCategory(updatedDetails.getCategory());

                    // --- New Text Fields (The ones that were missing!) ---
                    existingProduct.setSummary(updatedDetails.getSummary());
                    existingProduct.setScent(updatedDetails.getScent());
                    existingProduct.setOccasion(updatedDetails.getOccasion());

                    // --- Pricing & Inventory ---
                    existingProduct.setPrice(updatedDetails.getPrice());
                    existingProduct.setDiscountedPrice(updatedDetails.getDiscountedPrice());
                    existingProduct.setStock(updatedDetails.getStock());
                    existingProduct.setMinStockLevel(updatedDetails.getMinStockLevel());

                    // --- Media ---
                    existingProduct.setImageUrl(updatedDetails.getImageUrl());
                    existingProduct.setImages(updatedDetails.getImages()); // Gallery

                    // --- Rich Data Structures ---
                    existingProduct.setVariants(updatedDetails.getVariants());
                    existingProduct.setProductStory(updatedDetails.getProductStory());
                    existingProduct.setFeatures(updatedDetails.getFeatures());
                    existingProduct.setScentNotes(updatedDetails.getScentNotes());

                    // --- Toggles/Booleans ---
                    // Note: getters for booleans often follow 'isField()' or 'getField()' depending on Lombok config
                    // Assuming Lombok @Data standard:
                    existingProduct.setActive(updatedDetails.isActive());
                    existingProduct.setFeatured(updatedDetails.isFeatured());
                    existingProduct.setOnSale(updatedDetails.isOnSale());
                    existingProduct.setTaxIncluded(updatedDetails.isTaxIncluded());
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

                        }
                    });
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    public Product addRating(String productId, String userEmail, int stars, String comment) {
        Product product = getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Rating newRating = new Rating();
        newRating.setUserId(user.getId().toString());
        newRating.setUserName(user.getFirstName() + " " + user.getLastName());
        newRating.setStars(stars);
        newRating.setComment(comment);

        product.getRatings().add(newRating);

        product.setTotalReviews(product.getRatings().size());

        double average = product.getRatings().stream()
                .mapToInt(Rating::getStars)
                .average()
                .orElse(0.0);

        product.setAverageRating(Math.round(average * 10.0) / 10.0);

        return productRepository.save(product);
    }

    public Page<Product> getAllProducts(int page, int size, String search) {
        // Create a "page request" (Page 0 is the first page)
        Pageable pageable = PageRequest.of(page, size);

        if (search != null && !search.isEmpty()) {
            // If searching, use the search method
            return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(search, pageable);
        } else {
            // Otherwise, just return the page
            return productRepository.findByIsActiveTrue(pageable);
        }
    }

    public Page<Product> searchProducts(String keyword, String category, Double minPrice, Double maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

Autowired        String searchKey = (keyword != null) ? keyword : "";
        String catKey = (category != null && !category.equals("All")) ? category : ""; // "All" or null means matches everything
        double min = (minPrice != null) ? minPrice : 0.0;
        double max = (maxPrice != null) ? maxPrice : 1000000.0; // High default max

        String finalCatRegex = catKey.isEmpty() ? "" : "^" + catKey + "$";

        return productRepository.searchProducts(searchKey, finalCatRegex, min, max, pageable);
    }

}
