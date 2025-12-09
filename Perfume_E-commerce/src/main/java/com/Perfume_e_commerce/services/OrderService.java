package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.*;
import com.Perfume_e_commerce.models.marketing.Notification;
import com.Perfume_e_commerce.models.marketing.Promotion;
import com.Perfume_e_commerce.models.order.Cart;
import com.Perfume_e_commerce.models.order.CartItem;
import com.Perfume_e_commerce.models.order.Order;
import com.Perfume_e_commerce.models.order.OrderItem;
import com.Perfume_e_commerce.models.product.Product;
import com.Perfume_e_commerce.models.user.Address;
import com.Perfume_e_commerce.models.user.User;
import com.Perfume_e_commerce.models.inventory.InventoryLog; // Import the InventoryLog model
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private InventoryLogRepository inventoryLogRepository;

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
            Promotion promo = promotionService.validatePromotion(promoCode);

            discountAmount = totalAmount * (promo.getDiscountPercent() / 100.0);
            totalAmount = totalAmount - discountAmount;
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(new ObjectId(cartItem.getProductId()))
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductId()));

            // Check Stock
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            // Reduce Stock
            int oldStock = product.getStock();
            int quantityToReduce = cartItem.getQuantity();
            int newStock = oldStock - quantityToReduce;

            product.setStock(newStock);
            productRepository.save(product);

            InventoryLog log = new InventoryLog(
                    product.getId(),
                    "SALE",
                    -quantityToReduce,
                    newStock
            );
            inventoryLogRepository.save(log);

            if (product.getStock() <= product.getMinStockLevel()) {
                createLowStockNotification(product);
            }

            // Add to Order List (Snapshotted price)
            OrderItem orderItem = new OrderItem(
                    cartItem.getProductId(),
                    product.getName(),
                    cartItem.getQuantity(),
                    product.getPrice().doubleValue()
            );
            orderItems.add(orderItem);
        }

        Order newOrder = new Order();
        newOrder.setUserId(userId);
        newOrder.setItems(orderItems);
        newOrder.setTotalAmount(cart.getTotalPrice());
        newOrder.setShippingAddress(shippingAddress);
        newOrder.setStatus("CONFIRMED");
        newOrder.setPaymentStatus("PAID");
        newOrder.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        newOrder.setTotalAmount(totalAmount);
        newOrder.setDiscountAmount(discountAmount);
        newOrder.setPromoCodeUsed(promoCode);

        Order savedOrder = orderRepository.save(newOrder);

        cartService.clearCart(userId);

        return savedOrder;
    }

    private void createLowStockNotification(Product product) {
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