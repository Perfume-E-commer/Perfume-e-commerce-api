package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.InventoryLogRepository;
import com.Perfume_e_commerce.Repositories.NotificationRepository;
import com.Perfume_e_commerce.Repositories.OrderRepository;
import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.response.BillingResponse;
import com.Perfume_e_commerce.dto.response.DashboardStatsResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import java.time.LocalDate;
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

    @Autowired
    private NotificationService notificationService;

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

            discountAmount = totalAmount * (promo.getDiscountPercentage() / 100.0);
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
        newOrder.setEstimatedDelivery(LocalDate.now().plusDays(5));

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

            notificationService.createNotification(
                    admin.getId().toString(),
                    message,
                    "STOCK_ALERT"
            );
        }
    }

    public Order updateOrderStatus(String orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<String> validStatuses = List.of("CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(newStatus)) {
            throw new RuntimeException("Invalid status: " + newStatus);
        }

        String oldStatus = order.getStatus();

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        if (!oldStatus.equals(newStatus)) {
            String message = "";

            if ("SHIPPED".equals(newStatus)) {
                message = "Your order #" + order.getOrderNumber() + " has been shipped! It will arrive soon.";
            } else if ("DELIVERED".equals(newStatus)) {
                message = "Your order #" + order.getOrderNumber() + " has been delivered. Enjoy your scent!";
            } else if ("CANCELLED".equals(newStatus)) {
                message = "Your order #" + order.getOrderNumber() + " has been cancelled. Contact support for more details.";
            }

            // Only create notification if we have a message for this status change
            if (!message.isEmpty()) {
                notificationService.createNotification(
                        order.getUserId(),
                        message,
                        "ORDER_UPDATE"
                );
            }
        }

        return updatedOrder;
    }

    public List<BillingResponse> getBillingRecords() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream().map(order -> {
            String email = "Unknown";

            if (order.getUserId() != null) {
                email = userRepository.findById(new ObjectId(order.getUserId()))
                        .map(User::getEmail)
                        .orElse("Deleted User");
            }

            return BillingResponse.builder()
                    .orderId(order.getId().toString())
                    .orderNumber(order.getOrderNumber())
                    .customerEmail(email)
                    .totalAmount(BigDecimal.valueOf(order.getTotalAmount()))
                    .paymentStatus(order.getPaymentStatus()) // Ensure Order model has this (default "PAID")
                    .date(order.getCreatedAt()) // Assuming Order uses LocalDateTime
                    .build();
        }).collect(Collectors.toList());
    }

    public DashboardStatsResponse getDashboardStats() {
        List<Order> allOrders = orderRepository.findAll();

        // 1. Total Sales (Sum of totalAmount for non-cancelled orders)
        double totalSales = allOrders.stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();

        // 2. Total Orders
        long totalOrders = allOrders.size();

        // 3. Pending & Canceled Counts
        long pendingCount = allOrders.stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.getStatus()) || "CONFIRMED".equalsIgnoreCase(o.getStatus()))
                .count();

        long canceledCount = allOrders.stream()
                .filter(o -> "CANCELLED".equalsIgnoreCase(o.getStatus()))
                .count();

        return new DashboardStatsResponse(totalSales, totalOrders, pendingCount, canceledCount);
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public Page<Order> getAllOrders(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Order> orders;

        if (search != null && !search.isEmpty()) {
            orders = orderRepository.searchOrders(search, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        orders.forEach(order -> {
            if (order.getUserId() != null) {
                try {
                    userRepository.findById(new ObjectId(order.getUserId())).ifPresent(user -> {
                        order.setUserEmail(user.getEmail());
                    });
                } catch (Exception e) {
                }
            }
        });

        return orders;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByUser(String email) {
        return orderRepository.findByUserEmail(email);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
}
