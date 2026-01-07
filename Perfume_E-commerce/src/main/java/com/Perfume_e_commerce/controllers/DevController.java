package com.Perfume_e_commerce.controllers;

import com.Perfume_e_commerce.Repositories.*;
import com.Perfume_e_commerce.models.marketing.Notification;
import com.Perfume_e_commerce.models.order.Cart;
import com.Perfume_e_commerce.models.order.Order;
import com.Perfume_e_commerce.models.product.Category;
import com.Perfume_e_commerce.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dev")
public class DevController {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // GET /api/dev/orders
    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // GET /api/dev/carts
    @GetMapping("/carts")
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    // GET /api/dev/users
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // GET /api/dev/notifications
    @GetMapping("/notifications")
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    // GET /api/dev/categories
    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
