package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.CartRepository;
import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.dto.PlaceOrderRequest;
import com.Perfume_e_commerce.models.order.Cart;
import com.Perfume_e_commerce.models.order.CartItem;
import com.Perfume_e_commerce.models.product.Product;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public Cart getCartByUserId(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    public Cart addToCart(String userId, String productId, int quantity, String size) {
        Cart cart = getCartByUserId(userId);
        Product product = productRepository.findById(new ObjectId(productId))
                .orElseThrow(() -> new RuntimeException("Product not found"));

        double finalPrice = product.getPrice().doubleValue();
        String finalImageUrl = product.getImageUrl();

        if (size != null && !size.isEmpty()) {
            boolean variantExists = false;
            if (product.getVariants() != null) {
                variantExists = product.getVariants().stream()
                        .anyMatch(v -> v.getSize().equalsIgnoreCase(size));
            }

            if (!variantExists) {
                throw new RuntimeException("Variant '" + size + "' does not exist for this product.");
            }
        }

        if (size != null && !size.isEmpty() && product.getVariants() != null) {
            var variantOpt = product.getVariants().stream()
                    .filter(v -> v.getSize().equalsIgnoreCase(size))
                    .findFirst();

            if (variantOpt.isPresent()) {
                var variant = variantOpt.get();
                finalPrice = variant.getPrice().doubleValue();

                if (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
                    finalImageUrl = variant.getImageUrl();
                }
            }
        }

        String targetImageUrl = finalImageUrl;

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId)
                        && (item.getSize() == null || item.getSize().equals(size)))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
            existingItem.get().setImageUrl(targetImageUrl);
            existingItem.get().setCategory(product.getCategory());
        } else {
            CartItem newItem = new CartItem(
                    productId,
                    product.getName(),
                    quantity,
                    finalPrice,
                    size,
                    targetImageUrl 
            );
            newItem.setCategory(product.getCategory());
            cart.getItems().add(newItem);
        }

        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    public void removeItemsFromCart(String userId, List<PlaceOrderRequest.OrderItemRequest> itemsToRemove) {
        Cart cart = getCartByUserId(userId);
        if (cart != null && cart.getItems() != null) {

            cart.getItems().removeIf(cartItem -> itemsToRemove.stream().anyMatch(remove ->
                    remove.getProductId().equals(cartItem.getProductId()) &&
                            (
                                    (remove.getSize() == null && cartItem.getSize() == null) ||
                                            (remove.getSize() != null && remove.getSize().equals(cartItem.getSize()))
                            )
            ));

            cart.calculateTotal();
            cartRepository.save(cart);
        }
    }

    public Cart removeFromCart(String userId, String productId, String size) {
        Cart cart = getCartByUserId(userId);

        cart.getItems().removeIf(item -> {
            boolean idMatch = item.getProductId().equals(productId);

            String itemSize = (item.getSize() == null) ? "" : item.getSize();
            String paramSize = (size == null) ? "" : size;

            boolean sizeMatch = itemSize.equalsIgnoreCase(paramSize);

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