package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.AddToCartRequest;
import com.Perfume_e_commerce.models.order.Cart;
import com.Perfume_e_commerce.services.CartService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository; 

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();

            return userRepository.findByEmail(email)
                    .map(user -> user.getId().toString())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        }
        throw new RuntimeException("User not authenticated");
    }

    @GetMapping
    public ResponseEntity<Cart> getCart() {
        String userId = getCurrentUserId();
        Cart cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestBody AddToCartRequest request) {
        String userId = getCurrentUserId();
        Cart updatedCart = cartService.addToCart(
                userId,
                request.getProductId(),
                request.getQuantity(),
                request.getSize()
        );
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeFromCart(
            @PathVariable String productId,
            @RequestParam(required = false) String size) { 

        String userId = getCurrentUserId();
        Cart updatedCart = cartService.removeFromCart(userId, productId, size);
        return ResponseEntity.ok(updatedCart);
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<Cart> updateQuantity(
            @PathVariable String productId,
            @RequestBody Map<String, Object> payload) {

        String userId = getCurrentUserId();
        int quantity = (int) payload.get("quantity");
        String size = (String) payload.get("size"); 

        Cart updatedCart = cartService.updateQuantity(userId, productId, quantity, size);
        return ResponseEntity.ok(updatedCart);
    }
}
