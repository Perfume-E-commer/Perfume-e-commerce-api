package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.InventoryLogRepository;
import com.Perfume_e_commerce.Repositories.NotificationRepository;
import com.Perfume_e_commerce.Repositories.OrderRepository;
import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.models.marketing.Notification;
import com.Perfume_e_commerce.models.marketing.Promotion;
import com.Perfume_e_commerce.models.order.Cart;
import com.Perfume_e_commerce.models.order.CartItem;
import com.Perfume_e_commerce.models.order.Order;
import com.Perfume_e_commerce.models.order.OrderItem;
import com.Perfume_e_commerce.models.product.Product;
import com.Perfume_e_commerce.models.product.ProductVariant;
import com.Perfume_e_commerce.models.inventory.InventoryLog;
import com.Perfume_e_commerce.models.user.Address;
import com.Perfume_e_commerce.models.user.User;
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

            int quantityToReduce = cartItem.getQuantity();
            int currentVariantStock = 0;
            int currentVariantMinStock = product.getMinStockLevel();
            boolean isVariant = cartItem.getSize() != null && !cartItem.getSize().isEmpty();

            if (isVariant) {
                ProductVariant variant = product.getVariantBySize(cartItem.getSize())
                        .orElseThrow(() -> new RuntimeException("Variant not found: " + cartItem.getSize()));

                if (variant.getStock() < quantityToReduce) {
                    throw new RuntimeException("Not enough stock for variant: " + variant.getSize());
                }

                variant.setStock(variant.getStock() - quantityToReduce);
                product.recalculateTotalStock();
                currentVariantStock = variant.getStock();
                currentVariantMinStock = variant.getMinStock();

            } else {
                if (product.getStock() < quantityToReduce) {
                    throw new RuntimeException("Not enough stock for product: " + product.getName());
                }
                product.setStock(product.getStock() - quantityToReduce);
                currentVariantStock = product.getStock();
            }

            productRepository.save(product);

            String logProductId = product.getId() + (isVariant ? " (" + cartItem.getSize() + ")" : "");

            InventoryLog log = new InventoryLog(
                    product.getId(),
                    "SALE" + (isVariant ? " - " + cartItem.getSize() : ""),
                    -quantityToReduce,
                    currentVariantStock
            );
            inventoryLogRepository.save(log);

            if (currentVariantStock <= currentVariantMinStock) {
                createLowStockNotification(product, isVariant ? cartItem.getSize() : null);
            }

            OrderItem orderItem = new OrderItem(
                    cartItem.getProductId(),
                    product.getName() + (isVariant ? " (" + cartItem.getSize() + ")" : ""),
                    cartItem.getQuantity(),
                    cartItem.getPrice()
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

    private void createLowStockNotification(Product product, String variantSize) {
        List<User> admins = userRepository.findByRole("ADMIN");

        for (User admin : admins) {
            String itemName = product.getName() + (variantSize != null ? " (" + variantSize + ")" : "");

            int currentStock = (variantSize != null)
                    ? product.getVariantBySize(variantSize).map(ProductVariant::getStock).orElse(0)
                    : product.getStock();

            String message = "⚠️ Low Stock Alert: " + itemName + " is down to " + currentStock + " units.";

            Notification notification = new Notification(
                    admin.getId().toString(),
                    "STOCK_ALERT",
                    message
            );
            notificationRepository.save(notification);
        }
    }

    public Order updateOrderStatus(String orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<String> validStatuses = List.of("CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(newStatus)) {
            throw new RuntimeException("Invalid status: " + newStatus);
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        if ("SHIPPED".equals(newStatus)) {
            String message = "Good news! Your order #" + order.getOrderNumber() + " has been shipped.";
            Notification notification = new Notification(
                    order.getUserId(),
                    "ORDER_UPDATE",
                    message
            );
            notificationRepository.save(notification);
        }

        return updatedOrder;
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
