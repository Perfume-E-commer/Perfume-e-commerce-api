package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.models.Product;
import com.Perfume_e_commerce.models.User;
import com.Perfume_e_commerce.models.Order;
import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.Repositories.OrderRepository;
import com.Perfume_e_commerce.entity.ProductEntity;
import com.Perfume_e_commerce.entity.UserEntity;
import com.Perfume_e_commerce.entity.OrderEntity;
import com.Perfume_e_commerce.jpa.ProductJpaRepository;
import com.Perfume_e_commerce.jpa.UserJpaRepository;
import com.Perfume_e_commerce.jpa.OrderJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SyncService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Transactional
    public void syncMongoToSqlite() {
        try {
            // Sync Products
            List<Product> mongoProducts = productRepository.findAll();
            List<ProductEntity> productEntities = mongoProducts.stream()
                    .map(this::convertToProductEntity)
                    .collect(Collectors.toList());
            productJpaRepository.saveAll(productEntities);
            System.out.println("✅ Synced " + productEntities.size() + " products to SQLite");

            // Sync Users
            List<User> mongoUsers = userRepository.findAll();
            List<UserEntity> userEntities = mongoUsers.stream()
                    .map(this::convertToUserEntity)
                    .collect(Collectors.toList());
            userJpaRepository.saveAll(userEntities);
            System.out.println("✅ Synced " + userEntities.size() + " users to SQLite");

            // Sync Orders
            List<Order> mongoOrders = orderRepository.findAll();
            List<OrderEntity> orderEntities = mongoOrders.stream()
                    .map(this::convertToOrderEntity)
                    .collect(Collectors.toList());
            orderJpaRepository.saveAll(orderEntities);
            System.out.println("✅ Synced " + orderEntities.size() + " orders to SQLite");

        } catch (Exception e) {
            System.err.println("❌ Error during sync: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ProductEntity convertToProductEntity(Product product) {
        return ProductEntity.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .minStockLevel(product.getMinStockLevel())
                .isActive(product.isActive())
                .isFeatured(product.getIsFeatured())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private UserEntity convertToUserEntity(User user) {
        // Convert LocalDateTime to Date
        Date createDate = user.getCreatedAt() != null 
            ? java.sql.Timestamp.valueOf(user.getCreatedAt()) 
            : new Date();
            
        return UserEntity.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createAt(createDate)  // Note: field name is "createAt" not "createdAt"
                .build();
    }

    private OrderEntity convertToOrderEntity(Order order) {
        // Convert Address to String if it exists
        String addressStr = order.getShippingAddress() != null
                ? order.getShippingAddress().toString()
                : null;

        return OrderEntity.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalPrice(java.math.BigDecimal.valueOf(order.getTotalAmount()))
                .status(order.getStatus())
                .shippingAddress(addressStr)
                .paymentMethod(order.getPaymentStatus())
                .orderDate(order.getCreatedAt())
                .deliveryDate(null) // Not in MongoDB model
                .build();
    }

    @Scheduled(fixedRate = 30000) // every 30 seconds
    public void autoSync() {
        syncMongoToSqlite();
        System.out.println("✅ Auto sync MongoDB -> SQLite executed");
    }

}
