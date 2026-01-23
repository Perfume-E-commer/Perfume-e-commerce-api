package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.OrderRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.response.CustomerDetailResponse;
import com.Perfume_e_commerce.dto.response.CustomerListItemResponse;
import com.Perfume_e_commerce.dto.response.CustomerStatsResponse;
import com.Perfume_e_commerce.models.order.Order;
import com.Perfume_e_commerce.models.user.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

        private final UserRepository userRepository;
        private final OrderRepository orderRepository;

        public Page<CustomerListItemResponse> searchCustomersWithAnalytics(String search, int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<User> userPage;

                if (search != null && !search.trim().isEmpty()) {
                        userPage = userRepository.searchUsers(search, pageable);
                } else {
                        userPage = userRepository.findAll(pageable);
                }

                List<CustomerListItemResponse> content = userPage.getContent().stream().map(user -> {
                        String uid = user.getId().toString();

                        List<Order> userOrders = orderRepository.findByUserId(uid);

                        long orderCount = userOrders.size();
                        double totalSpent = userOrders.stream()
                                .mapToDouble(o -> {
                                        // Handle potential nulls safely
                                        if (o.getTotal() != null) return o.getTotal().doubleValue();
                                        return o.getTotalAmount();
                                })
                                .sum();

                        String lastActive = userOrders.stream()
                                .map(Order::getCreatedAt)
                                .filter(Objects::nonNull)
                                .max(LocalDateTime::compareTo)
                                .map(LocalDateTime::toString)
                                .orElse(null);

                        String joinedAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : "";

                        String phone = user.getPhoneNumber();
                        if ((phone == null || phone.isEmpty()) && user.getAddresses() != null && !user.getAddresses().isEmpty()) {
                                phone = user.getAddresses().get(0).getPhoneNumber();
                        }

                        return new CustomerListItemResponse(
                                uid,
                                user.getFirstName(),
                                user.getLastName(),
                                user.getEmail(),
                                phone,
                                true,
                                joinedAt,
                                orderCount,
                                totalSpent,
                                lastActive
                        );
                }).collect(Collectors.toList());

                return new PageImpl<>(content, pageable, userPage.getTotalElements());
        }

        public CustomerStatsResponse getCustomerStats() {
                long totalCustomers = userRepository.count();
                List<Order> allOrders = orderRepository.findAll();

                LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
                long newThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);

                double totalRevenue = allOrders.stream()
                                .mapToDouble(o -> o.getTotal() != null ? o.getTotal().doubleValue()
                                                : o.getTotalAmount())
                                .sum();

                double avgSpend = totalCustomers > 0 ? totalRevenue / totalCustomers : 0.0;

                Map<String, Long> ordersPerUser = allOrders.stream()
                                .filter(o -> o.getUserId() != null)
                                .collect(Collectors.groupingBy(Order::getUserId, Collectors.counting()));

                long returningCustomers = ordersPerUser.values().stream()
                                .filter(count -> count > 1)
                                .count();

                return new CustomerStatsResponse(
                        totalCustomers,
                        avgSpend,
                        returningCustomers,
                        newThisMonth
                );
        }

        public CustomerDetailResponse getCustomerDetails(String userId) {
                User user = userRepository.findById(new ObjectId(userId))
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<Order> userOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

                List<CustomerDetailResponse.OrderHistoryItem> historyItems = userOrders.stream()
                                .map(order -> new CustomerDetailResponse.OrderHistoryItem(
                                                order.getId(), order.getOrderNumber(),
                                                order.getCreatedAt() != null ? order.getCreatedAt().toString() : "",
                                                order.getTotal() != null ? order.getTotal().doubleValue()
                                                                : order.getTotalAmount(),
                                                order.getStatus(),
                                                order.getItems() != null ? order.getItems().size() : 0))
                                .collect(Collectors.toList());

                return new CustomerDetailResponse(user, historyItems);
        }

        public byte[] exportCustomersToCSV() {
                List<User> users = userRepository.findAll();
                List<Order> allOrders = orderRepository.findAll();

                Map<String, List<Order>> ordersByUser = allOrders.stream()
                                .filter(o -> o.getUserId() != null)
                                .collect(Collectors.groupingBy(Order::getUserId));

                StringBuilder csv = new StringBuilder();
                csv.append("User ID,First Name,Last Name,Email,Joined Date,Total Orders,Total Spent,Last Active\n");

                for (User user : users) {
                        String uid = user.getId().toString();
                        List<Order> userOrders = ordersByUser.getOrDefault(uid, Collections.emptyList());

                        long count = userOrders.size();
                        double total = userOrders.stream()
                                        .mapToDouble(o -> o.getTotal() != null ? o.getTotal().doubleValue()
                                                        : o.getTotalAmount())
                                        .sum();

                        String lastActive = userOrders.stream()
                                        .map(Order::getCreatedAt)
                                        .filter(Objects::nonNull)
                                        .max(LocalDateTime::compareTo)
                                        .map(LocalDateTime::toString)
                                        .orElse("N/A");

                        csv.append(String.join(",",
                                        uid,
                                        escapeCsv(user.getFirstName()),
                                        escapeCsv(user.getLastName()),
                                        escapeCsv(user.getEmail()),
                                        user.getCreatedAt() != null ? user.getCreatedAt().toString() : "",
                                        String.valueOf(count),
                                        String.format("%.2f", total),
                                        lastActive));
                        csv.append("\n");
                }

                return csv.toString().getBytes(StandardCharsets.UTF_8);
        }

        private String escapeCsv(String data) {
                if (data == null)
                        return "";
                String escaped = data.replaceAll("\\R", " "); // Remove newlines
                if (data.contains(",") || data.contains("\"") || data.contains("'")) {
                        data = data.replace("\"", "\"\"");
                        escaped = "\"" + data + "\"";
                }
                return escaped;
        }
}