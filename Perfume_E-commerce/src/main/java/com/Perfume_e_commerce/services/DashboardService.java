package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.OrderRepository;
import com.Perfume_e_commerce.Repositories.ProductRepository;
import com.Perfume_e_commerce.Repositories.PromotionRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.response.AdminDashboardResponse;
import com.Perfume_e_commerce.models.order.Order;
import com.Perfume_e_commerce.models.product.Product;
import com.Perfume_e_commerce.models.marketing.Promotion;
import com.Perfume_e_commerce.models.user.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId; // Ensure ObjectId is available if needed
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;

    public AdminDashboardResponse getDashboardData() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Order> allOrders = orderRepository.findAll();

        List<Order> recentOrdersList = allOrders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());

        long totalOrders30d = recentOrdersList.size();

        double revenue30d = recentOrdersList.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        long activeCustomers = allOrders.stream()
                .map(Order::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        List<Product> allProducts = productRepository.findAll();
        long lowStockCount = allProducts.stream()
                .filter(p -> p.getStock() <= 5)
                .count();

        Map<LocalDate, AdminDashboardResponse.DailySalesData> chartMap = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (Order order : recentOrdersList) {
            if (order.getCreatedAt() == null) continue;

            LocalDate date = order.getCreatedAt().toLocalDate();

            if (chartMap.containsKey(date)) {
                AdminDashboardResponse.DailySalesData data = chartMap.get(date);
                double amount = order.getTotalAmount();
                data.setRevenue(data.getRevenue() + amount);
                data.setOrderCount(data.getOrderCount() + 1);
            }
        }
        List<AdminDashboardResponse.DailySalesData> salesChart = new ArrayList<>(chartMap.values());
        Collections.sort(salesChart, Comparator.comparing(d -> LocalDate.parse(d.getDate() + " " + LocalDate.now().getYear(), DateTimeFormatter.ofPattern("MMM dd yyyy"))));

        List<AdminDashboardResponse.RecentOrder> recentOrders = allOrders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(this::mapToRecentOrder)
                .collect(Collectors.toList());

        List<AdminDashboardResponse.LowStockItem> lowStockItems = allProducts.stream()
                .filter(p -> p.getStock() <= 5)
                .limit(5)
                .map(p -> new AdminDashboardResponse.LowStockItem(
                        p.getId(),
                        p.getName(),
                        p.getStock(),
                        getPrimaryImage(p)
                ))
                .collect(Collectors.toList());

        List<Promotion> allPromos = promotionRepository.findAll();
        List<AdminDashboardResponse.ActivePromotion> activePromotions = allPromos.stream()
                .filter(p -> p.isActive())
                .limit(5)
                .map(p -> new AdminDashboardResponse.ActivePromotion(
                        p.getId(),
                        p.getCode(),
                        p.getDiscountPercentage(),
                        p.getValidUntil() != null ? p.getValidUntil().toString() : "N/A"
                ))
                .collect(Collectors.toList());

        return new AdminDashboardResponse(
                totalOrders30d,
                revenue30d,
                lowStockCount,
                activeCustomers,
                salesChart,
                recentOrders,
                lowStockItems,
                activePromotions
        );
    }

    private AdminDashboardResponse.RecentOrder mapToRecentOrder(Order order) {
        String customerName = "Guest";
        try {
            if (order.getUserId() != null) {
                Optional<User> userOpt = userRepository.findById(new ObjectId(order.getUserId()));

                if (userOpt.isPresent()) {
                    customerName = userOpt.get().getFirstName() + " " + userOpt.get().getLastName();
                } else {
                    customerName = "User #" + order.getUserId().substring(0, 5) + "..."; // Fallback format
                }
            }
        } catch (Exception e) {
            customerName = "Unknown";
        }

        return new AdminDashboardResponse.RecentOrder(
                order.getId(),
                order.getId(), // Or order.getOrderNumber() if available
                customerName,
                order.getTotal() != null ? order.getTotal().doubleValue() : 0.0,
                order.getStatus(),
                order.getCreatedAt() != null ? order.getCreatedAt().toString() : ""
        );
    }

    private String getPrimaryImage(Product p) {
        if (p.getImages() != null && !p.getImages().isEmpty()) {
            return p.getImages().get(0);
        }
        return null;
    }
}