package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.InventoryLogRepository;
import com.Perfume_e_commerce.Repositories.NotificationRepository;
import com.Perfume_e_commerce.Repositories.OrderRepository;
import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.PlaceOrderRequest;
import com.Perfume_e_commerce.dto.response.*;
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
import java.util.Optional;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public Order placeOrder(String userId, String userEmail, Address shippingAddress, String promoCode, List<PlaceOrderRequest.OrderItemRequest> selectedItems) {

        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot place order.");
        }

        List<CartItem> itemsToProcess = cart.getItems();

        if (selectedItems != null && !selectedItems.isEmpty()) {
            itemsToProcess = cart.getItems().stream()
                    .filter(cartItem -> selectedItems.stream().anyMatch(selected ->
                            selected.getProductId().equals(cartItem.getProductId()) &&
                                    (
                                            (selected.getSize() == null && cartItem.getSize() == null) ||
                                                    (selected.getSize() != null && selected.getSize().equals(cartItem.getSize()))
                                    )
                    ))
                    .collect(Collectors.toList());

            if (itemsToProcess.isEmpty()) {
                throw new RuntimeException("No valid items selected for checkout.");
            }
        }

        double subtotal = itemsToProcess.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        double discountAmount = 0.0;
        double finalTotal = subtotal;

        if (promoCode != null && !promoCode.isEmpty()) {
            Promotion promo = promotionService.validatePromotion(promoCode);
            discountAmount = subtotal * (promo.getDiscountPercentage() / 100.0);
            finalTotal = subtotal - discountAmount;
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : itemsToProcess) {
            Product product = productRepository.findById(new ObjectId(cartItem.getProductId()))
                    .orElseThrow(() -> new RuntimeException("Product not found: " + cartItem.getProductId()));

            int quantityToReduce = cartItem.getQuantity();

            String finalImage = product.getImageUrl();
            if (finalImage == null && product.getImages() != null && !product.getImages().isEmpty()) {
                finalImage = product.getImages().get(0);
            }

            int currentStock = 0;
            boolean isVariant = cartItem.getSize() != null && !cartItem.getSize().isEmpty();

            if (isVariant) {
                Optional<ProductVariant> variantOpt = product.getVariantBySize(cartItem.getSize());

                if (variantOpt.isPresent()) {
                    ProductVariant variant = variantOpt.get();
                    if (variant.getStock() < quantityToReduce) {
                        throw new RuntimeException("Not enough stock for variant: " + variant.getSize());
                    }
                    variant.setStock(variant.getStock() - quantityToReduce);
                    product.recalculateTotalStock();
                    currentStock = variant.getStock();
                    if (variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
                        finalImage = variant.getImageUrl();
                    }
                } else {
                    System.err.println("Warning: Variant '" + cartItem.getSize() + "' not found for product '" + product.getName() + "'. Deducting from main stock.");
                    if (product.getStock() < quantityToReduce) {
                        throw new RuntimeException("Not enough stock for product: " + product.getName());
                    }
                    product.setStock(product.getStock() - quantityToReduce);
                    currentStock = product.getStock();
                }
            } else {
                if (product.getStock() < quantityToReduce) {
                    throw new RuntimeException("Not enough stock for product: " + product.getName());
                }
                product.setStock(product.getStock() - quantityToReduce);
                currentStock = product.getStock();
            }

            productRepository.save(product);

            checkAndNotifyLowStock(product, isVariant ? cartItem.getSize() : null);

            // Log Inventory Movement
            InventoryLog log = new InventoryLog(
                    product.getId(),
                    "SALE" + (isVariant ? " - " + cartItem.getSize() : ""),
                    -quantityToReduce,
                    currentStock
            );
            inventoryLogRepository.save(log);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(product.getName() + (isVariant ? " (" + cartItem.getSize() + ")" : ""));
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setImageUrl(finalImage);

            orderItem.setBrand(product.getBrand());
            orderItem.setCategory(product.getCategory());
            orderItem.setOccasion(product.getOccasion());
            orderItem.setVariant(cartItem.getSize());

            orderItems.add(orderItem);
        }

        Order newOrder = new Order();
        newOrder.setUserId(userId);
        newOrder.setUserEmail(userEmail);
        newOrder.setItems(orderItems);

        newOrder.setSubtotal(subtotal);
        newOrder.setShippingCost(0.0);
        newOrder.setTotalAmount(finalTotal);
        newOrder.setDiscountAmount(discountAmount);
        newOrder.setPromoCodeUsed(promoCode);

        newOrder.setShippingAddress(shippingAddress);
        newOrder.setStatus("CONFIRMED");
        newOrder.setPaymentStatus("PAID");
        newOrder.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        newOrder.setEstimatedDelivery(LocalDate.now().plusDays(5));
        newOrder.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(newOrder);

        if (selectedItems == null || selectedItems.isEmpty()) {
            cartService.clearCart(userId);
        } else {
            cartService.removeItemsFromCart(userId, selectedItems);
        }

        return savedOrder;
    }

    private void createLowStockNotification(Product product, String variantSize) {
        List<User> admins = userRepository.findByRole("ADMIN");

        for (User admin : admins) {
            String itemName = product.getName() + (variantSize != null ? " (" + variantSize + ")" : "");
            String message = "⚠️ Low Stock Alert: " + itemName + " is running low.";

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

        List<String> validStatuses = List.of("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");

        if (!validStatuses.contains(newStatus)) {
            throw new RuntimeException("Invalid status: " + newStatus);
        }

        String oldStatus = order.getStatus();

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        if (!oldStatus.equals(newStatus)) {
            String message = "";
            if ("SHIPPED".equals(newStatus)) {
                message = "Your order #" + order.getOrderNumber() + " has been shipped!";
            } else if ("DELIVERED".equals(newStatus)) {
                message = "Your order #" + order.getOrderNumber() + " has been delivered.";
            } else if ("CANCELLED".equals(newStatus)) {
                message = "Your order #" + order.getOrderNumber() + " has been cancelled.";
            }

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
                    .paymentStatus(order.getPaymentStatus())
                    .date(order.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    public AdminDashboardResponse getDashboardStats() {
        List<Order> allOrders = orderRepository.findAll();

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<Order> last30DaysOrders = allOrders.stream()
                .filter(o -> o.getCreatedAt().toLocalDate().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());

        long totalOrders30d = last30DaysOrders.size();

        double revenue30d = last30DaysOrders.stream()
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(Order::getTotalAmount)
                .sum();

        long activeCustomers = last30DaysOrders.stream()
                .map(Order::getUserId)
                .distinct()
                .count();

        List<RecentOrder> recentOrders = allOrders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt())) // Newest first
                .limit(5)
                .map(order -> new RecentOrder(
                        order.getId(),
                        order.getOrderNumber(), // This ensures ORD-XXXX is sent
                        order.getShippingAddress() != null ? order.getShippingAddress().getFullName() : "Guest",
                        order.getTotalAmount(),
                        order.getStatus(),
                        order.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());

        List<DailySalesData> salesChart = new ArrayList<>();
        List<LowStockItem> lowStockItems = new ArrayList<>();
        List<ActivePromotion> activePromotions = new ArrayList<>();

        return new AdminDashboardResponse(
                (int) totalOrders30d,
                revenue30d,
                0,
                (int) activeCustomers,
                salesChart,
                recentOrders,
                lowStockItems,
                activePromotions
        );
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
                } catch (Exception e) {}
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

    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    private void checkAndNotifyLowStock(Product product, String variantSize) {
        int currentStock = 0;
        int threshold = product.getMinStockLevel();

        if (variantSize != null) {
            Optional<ProductVariant> variantOpt = product.getVariantBySize(variantSize);
            if (variantOpt.isPresent()) {
                ProductVariant v = variantOpt.get();
                currentStock = v.getStock();
                if (v.getMinStock() != null) {
                    threshold = v.getMinStock();
                }
            }
        } else {
            currentStock = product.getStock();
        }

        if (currentStock <= threshold) {
            createLowStockNotification(product, variantSize, currentStock, threshold);
        }
    }

    private void createLowStockNotification(Product product, String variantSize, int currentStock, int threshold) {
        List<User> admins = userRepository.findByRole("ADMIN");
        String itemName = product.getName() + (variantSize != null ? " (" + variantSize + ")" : "");

        String message = String.format(
                "⚠️ Low Stock Alert: %s is down to %d units (Threshold: %d). Please restock soon.",
                itemName, currentStock, threshold
        );

        for (User admin : admins) {
            notificationService.createNotification(
                    admin.getId().toString(),
                    message,
                    "STOCK_ALERT"
            );
        }
    }
}