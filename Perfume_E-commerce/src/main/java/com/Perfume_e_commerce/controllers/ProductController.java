package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.services.ProductService;
import com.Perfume_e_commerce.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product")
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
}
