package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.dto.request.AddRatingRequest;
import com.Perfume_e_commerce.dto.request.CreateProductRequest;
import com.Perfume_e_commerce.services.ProductService;
import com.Perfume_e_commerce.models.product.Product;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        if (search != null || category != null || minPrice != null || maxPrice != null) {
            return ResponseEntity.ok(productService.searchProducts(search, category, minPrice, maxPrice, page, size));
        }
        return ResponseEntity.ok(productService.getProductsForCustomer(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Optional<Product> product = productService.getProductById(id);

        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody CreateProductRequest request){
        Product product = mapRequestToProduct(request);
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
        product.setSummary(request.getSummary());
        product.setScent(request.getScent());
        product.setOccasion(request.getOccasion());
        product.setPrice(request.getPrice());
        product.setDiscountedPrice(request.getDiscountedPrice());
        product.setStock(request.getStock());
        product.setMinStockLevel(request.getMinStockLevel());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        product.setImages(request.getImages());
        product.setVariants(request.getVariants());
        product.setProductStory(request.getProductStory());
        product.setFeatures(request.getFeatures());
        product.setScentNotes(request.getScentNotes());

        if (request.getIsActive() != null) product.setActive(request.getIsActive());

        return product;
    }

}
