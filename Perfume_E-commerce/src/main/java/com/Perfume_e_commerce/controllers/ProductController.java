package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.dto.CreateProductRequest;
import com.Perfume_e_commerce.dto.ProductDTO;
import com.Perfume_e_commerce.dto.ProductFilterDTO;
import com.Perfume_e_commerce.services.ProductService;
import com.Perfume_e_commerce.models.Product;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductRequest request) {

        Product product = new Product();
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());

        /// I Included new fields
        product.setScent(request.getScent());
        product.setOccasion(request.getOccasion());
        product.setGender(request.getGender());
        product.setSummary(request.getSummary());
        product.setDiscountedPrice(request.getDiscountedPrice());
        product.setTaxIncluded(request.getTaxIncluded());
        product.setIsOnSale(request.getIsOnSale());
        product.setIsFeatured(request.getIsFeatured());

        Product saveProduct = productService.saveProduct(product);

        return ResponseEntity.ok(saveProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody CreateProductRequest request) {
        // Reuse the Request DTO since the fields are the same
        Product productDetails = new Product();
        productDetails.setName(request.getName());
        productDetails.setBrand(request.getBrand());
        productDetails.setDescription(request.getDescription());
        productDetails.setPrice(request.getPrice());
        productDetails.setStock(request.getStock());
        productDetails.setCategory(request.getCategory());
        productDetails.setImageUrl(request.getImageUrl());

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
    public ResponseEntity<?> patchProduct(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        Product updatedProduct = productService.patchProduct(id, updates);
        return ResponseEntity.ok(updatedProduct);
    }

}
