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

        public Page<CustomerListItemResponse> searchCustomersWithAnalytics(String search, int page, int size,
                        String orderCountFilter, String spendingTier) {
                // Load all users and orders then map, filter, and paginate server-side to
                // support richer filters
                List<User> allUsers = userRepository.findAll();
                List<Order> allOrders = orderRepository.findAll();

                Map<String, List<Order>> ordersByUser = allOrders.stream()
                                .filter(o -> o.getUserId() != null)
                                .collect(Collectors.groupingBy(Order::getUserId));

                // Map users to DTOs with analytics
                List<CustomerListItemResponse> mapped = allUsers.stream()
                                .map(user -> {
                                        String uid = user.getId().toString();
                                        List<Order> userOrders = ordersByUser.getOrDefault(uid,
                                                        Collections.emptyList());

                                        long orderCount = userOrders.size();
                                        double totalSpent = userOrders.stream()
                                                        .mapToDouble(o -> o.getTotal() != null
                                                                        ? o.getTotal().doubleValue()
                                                                        : o.getTotalAmount())
                                                        .sum();

                                        String lastActive = userOrders.stream()
                                                        .map(Order::getCreatedAt)
                                                        .filter(Objects::nonNull)
                                                        .max(LocalDateTime::compareTo)
                                                        .map(LocalDateTime::toString)
                                                        .orElse(null);

                                        return new CustomerListItemResponse(
                                                        uid, user.getFirstName(), user.getLastName(), user.getEmail(),
                                                        true,
                                                        user.getCreatedAt() != null ? user.getCreatedAt().toString()
                                                                        : "",
                                                        orderCount, totalSpent, lastActive);
                                })
                                .collect(Collectors.toList());

                // Apply text search if provided
                String q = (search != null) ? search.trim().toLowerCase() : "";
                java.util.stream.Stream<CustomerListItemResponse> stream = mapped.stream();
                if (!q.isEmpty()) {
                        stream = stream.filter(c -> (c.getFirstName() != null
                                        && c.getFirstName().toLowerCase().contains(q)) ||
                                        (c.getLastName() != null && c.getLastName().toLowerCase().contains(q)) ||
                                        (c.getEmail() != null && c.getEmail().toLowerCase().contains(q)));
                }

                // Apply order count filter
                if (orderCountFilter != null && !orderCountFilter.isEmpty()) {
                        switch (orderCountFilter) {
                                case "1":
                                        stream = stream.filter(c -> c.getOrdersCount() == 1);
                                        break;
                                case "2+":
                                        stream = stream.filter(c -> c.getOrdersCount() >= 2);
                                        break;
                                case "5+":
                                        stream = stream.filter(c -> c.getOrdersCount() >= 5);
                                        break;
                                default:
                                        // ignore unknown
                        }
                }

                // Apply spending tier filter
                if (spendingTier != null && !spendingTier.isEmpty()) {
                        switch (spendingTier) {
                                case "high":
                                        stream = stream.filter(c -> c.getTotalSpent() > 200.0);
                                        break;
                                case "medium":
                                        stream = stream.filter(
                                                        c -> c.getTotalSpent() >= 50.0 && c.getTotalSpent() <= 200.0);
                                        break;
                                case "low":
                                        stream = stream.filter(c -> c.getTotalSpent() < 50.0);
                                        break;
                                default:
                                        // ignore unknown
                        }
                }

                List<CustomerListItemResponse> filtered = stream.collect(Collectors.toList());

                // Pagination
                int fromIndex = page * size;
                int toIndex = Math.min(fromIndex + size, filtered.size());
                List<CustomerListItemResponse> pageContent = new ArrayList<>();
                if (fromIndex < filtered.size()) {
                        pageContent = filtered.subList(fromIndex, toIndex);
                }

                Pageable pageable = PageRequest.of(page, size);
                return new PageImpl<>(pageContent, pageable, filtered.size());
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

                return new CustomerStatsResponse(totalCustomers, avgSpend, returningCustomers, newThisMonth);
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