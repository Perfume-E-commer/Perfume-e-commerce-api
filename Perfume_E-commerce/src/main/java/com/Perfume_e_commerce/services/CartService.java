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

        if (size != null && !size.isEmpty() && product.getVariants() != null) {
            // Find the specific variant
            finalPrice = product.getVariants().stream()
                    .filter(v -> v.getSize().equalsIgnoreCase(size))
                    .findFirst()
                    .map(v -> v.getPrice().doubleValue())
                    .orElse(finalPrice);
        }

        // 3. Check if item (product + size) exists
        // We must check BOTH productId AND size now!
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId)
                        && (item.getSize() == null || item.getSize().equals(size)))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem(
                    productId,
                    product.getName(),
                    quantity,
                    finalPrice,
                    size
            );
            cart.getItems().add(newItem);
        }

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    public Cart removeFromCart(String userId, String productId) {
        Cart cart = getCartByUserId(userId);

        cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    public void clearCart(String userId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().clear();
        cart.setTotalPrice(0);
        cartRepository.save(cart);
    }
}