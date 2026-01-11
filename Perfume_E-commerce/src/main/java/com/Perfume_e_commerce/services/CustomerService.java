package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.OrderRepository;
import com.Perfume_e_commerce.Repositories.UserRepository;
import com.Perfume_e_commerce.dto.response.CustomerDetailResponse;
import com.Perfume_e_commerce.dto.response.CustomerStatsResponse;
import com.Perfume_e_commerce.models.order.Order;
import com.Perfume_e_commerce.models.user.User;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public CustomerStatsResponse getCustomerStats() {
        long totalCustomers = userRepository.count();
        List<Order> allOrders = orderRepository.findAll();

        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        long newThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);

        double totalRevenue = allOrders.stream()
                .map(Order::getTotal)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
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
                        order.getId(),
                        order.getId(), // Using ID as Order Number
                        order.getCreatedAt() != null ? order.getCreatedAt().toString() : "",
                        order.getTotal() != null ? order.getTotal().doubleValue() : 0.0, // FIX: BigDecimal -> double
                        order.getStatus(),
                        order.getItems() != null ? order.getItems().size() : 0
                ))
                .collect(Collectors.toList());

        return new CustomerDetailResponse(user, historyItems);
    }
}