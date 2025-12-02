package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.NotificationRepository;
import com.Perfume_e_commerce.Repositories.OrderRepository;
import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromotionService promotionService;

    @Transactional
    public Order placeOrder(String userId, Address shippingAddress, String promoCode) {
        
        Cart cart = cartService.getCartByUserId(userId);
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        double totalAmount = cart.getTotalPrice();
        double discountAmount = 0.0;

        if (promoCode != null && !promoCode.isEmpty()) {
            // Validate (will throw exception if invalid/expired)
            Promotion promo = promotionService.validatePromotion(promoCode);

            // Calculate discount
            discountAmount = totalAmount * (promo.getDiscountPercent() / 100.0);
            totalAmount = totalAmount - discountAmount;
        }
        
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId()) // Find by String ID directly if ID is String
                    // If your repo expects ObjectId, convert it: new ObjectId(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductId()));

            // Check Stock
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            // Reduce Stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            if (product.getStock() <= product.getMinStockLevel()) {
                createLowStockNotification(product);
            }

            // Add to Order List (Snapshotted price)
            OrderItem orderItem = new OrderItem(
                    cartItem.getProductId(),
                    product.getName(),
                    cartItem.getQuantity(),
                    product.getPrice().doubleValue() // Lock in the price at purchase time
            );
            orderItems.add(orderItem);
        }

        // 3. Create the Order Record
        Order newOrder = new Order();
        newOrder.setUserId(userId);
        newOrder.setItems(orderItems);
        newOrder.setTotalAmount(cart.getTotalPrice());
        newOrder.setShippingAddress(shippingAddress);
        newOrder.setStatus("CONFIRMED");
        newOrder.setPaymentStatus("PAID"); // Mocking successful payment
        newOrder.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        newOrder.setTotalAmount(totalAmount);     // The discounted price
        newOrder.setDiscountAmount(discountAmount);
        newOrder.setPromoCodeUsed(promoCode);

        Order savedOrder = orderRepository.save(newOrder);

        cartService.clearCart(userId);

        return savedOrder;
    }

    private void createLowStockNotification(Product product) {
        // Find all users with role "ADMIN"
        List<User> admins = userRepository.findByRole("ADMIN"); // You might need to add this method to UserRepository!

        for (User admin : admins) {
            String message = "⚠️ Low Stock Alert: " + product.getName() + " is down to " + product.getStock() + " units.";
            Notification notification = new Notification(
                    admin.getId().toString(), // Assuming ID is ObjectId, convert to String
                    "STOCK_ALERT",
                    message
            );
            notificationRepository.save(notification);
        }
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
