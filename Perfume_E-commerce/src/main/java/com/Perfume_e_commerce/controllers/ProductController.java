package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.dto.request.AddRatingRequest;
import com.Perfume_e_commerce.dto.request.CreateProductRequest;
import com.Perfume_e_commerce.services.ProductService;
import com.Perfume_e_commerce.models.product.Product;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllActiveProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Optional<Product> product = productService.getProductById(id);

        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin-test")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTest() {
        return "SUCCESS: You are an ADMIN!";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductRequest request){
        Product product = mapRequestToProduct(request); // Use helper method
        Product saveProduct = productService.saveProduct(product);

        return ResponseEntity.ok(saveProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody CreateProductRequest request) {
        Product productDetails = mapRequestToProduct(request); // Use helper method

        productDetails.setId(id);

        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        Product updatedProduct = productService.patchProduct(id, updates);
        return ResponseEntity.ok(updatedProduct);
    }

    @PostMapping("/{id}/ratings")
    public ResponseEntity<?> addRating(@PathVariable String id, @Valid @RequestBody AddRatingRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            Product updatedProduct = productService.addRating(
                    id,
                    email,
                    request.getStars(),
                    request.getComment()
            );
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Product mapRequestToProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setSummary(request.getSummary()); // New
        product.setScent(request.getScent());     // New
        product.setOccasion(request.getOccasion()); // New

        product.setPrice(request.getPrice());
        product.setDiscountedPrice(request.getDiscountedPrice()); // New

        product.setStock(request.getStock());
        product.setMinStockLevel(request.getMinStockLevel()); // New

        product.setCategory(request.getCategory());

        product.setImageUrl(request.getImageUrl());
        product.setImages(request.getImages()); // New

        product.setVariants(request.getVariants());
        product.setProductStory(request.getProductStory());
        product.setFeatures(request.getFeatures());
        product.setScentNotes(request.getScentNotes());

        // Handle Booleans (null check safety if needed, though Boolean defaults to null)
        if (request.getIsActive() != null) product.setActive(request.getIsActive());
        if (request.getIsFeatured() != null) product.setFeatured(request.getIsFeatured());
        if (request.getIsOnSale() != null) product.setOnSale(request.getIsOnSale());
        if (request.getTaxIncluded() != null) product.setTaxIncluded(request.getTaxIncluded());

        return product;
    }

}
