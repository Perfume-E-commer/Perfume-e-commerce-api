package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.CartRepository;
import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.models.order.Cart;
import com.Perfume_e_commerce.models.order.CartItem;
import com.Perfume_e_commerce.models.product.Product;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    public Cart addToCart(String userId, String productId, int quantity, String size) {
        Cart cart = getCartByUserId(userId);
        Product product = productRepository.findById(new ObjectId(productId))
                .orElseThrow(() -> new RuntimeException("Product not found"));

        double finalPrice = product.getPrice().doubleValue();
        String finalImageUrl = product.getImageUrl(); // Default to main image

        if (size != null && !size.isEmpty() && product.getVariants() != null) {
            var variantOpt = product.getVariants().stream()
                    .filter(v -> v.getSize().equalsIgnoreCase(size))
                    .findFirst();

            if (variantOpt.isPresent()) {
                var variant = variantOpt.get();
                finalPrice = variant.getPrice().doubleValue();

                // ✅ Fix: Use variant image if available, otherwise keep main image
                if (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
                    finalImageUrl = variant.getImageUrl();
                }
            }
        }

        String targetImageUrl = finalImageUrl; // Variable for lambda use

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId)
                        && (item.getSize() == null || item.getSize().equals(size)))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
            existingItem.get().setImageUrl(targetImageUrl);
        } else {
            CartItem newItem = new CartItem(
                    productId,
                    product.getName(),
                    quantity,
                    finalPrice,
                    size,
                    targetImageUrl 
            );
            cart.getItems().add(newItem);
        }

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    public Cart removeFromCart(String userId, String productId, String size) {
        Cart cart = getCartByUserId(userId);

        cart.getItems().removeIf(item -> {
            boolean idMatch = item.getProductId().equals(productId);

            boolean sizeMatch = (size == null && item.getSize() == null) ||
                    (size != null && size.equalsIgnoreCase(item.getSize()));

            return idMatch && sizeMatch;
        });

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    public void clearCart(String userId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
        cart.setTotalPrice(0);
        cartRepository.save(cart);
    }

    public Cart updateQuantity(String userId, String productId, int quantity, String size) {
        Cart cart = getCartByUserId(userId);

        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId) &&
                        ((size == null && item.getSize() == null) ||
                                (size != null && size.equalsIgnoreCase(item.getSize()))))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity)); 

        cart.calculateTotal();
        return cartRepository.save(cart);
    }
}