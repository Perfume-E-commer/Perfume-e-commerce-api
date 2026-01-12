package com.Perfume_e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {
    // 1. Top KPI Cards
    private long totalOrders30d;
    private double revenue30d;
    private long lowStockCount;
    private long activeCustomers;

    // 2. Sales Overview Chart (Last 30 Days)
    private List<DailySalesData> salesChart;

    // 3. Recent Activity Panels
    private List<RecentOrder> recentOrders;
    private List<LowStockItem> lowStockItems;
    private List<ActivePromotion> activePromotions;

    // --- Nested Helper Classes ---

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DailySalesData {
        private String date;    // e.g., "Jan 12"
        private double revenue; // e.g., 150.00
        private long orderCount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentOrder {
        private String id;
        private String orderNumber;
        private String customerName; // or email
        private double total;
        private String status;
        private String createdAt; // ISO String
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LowStockItem {
        private String id;
        private String name;
        private int stock;
        private String imageUrl; // Optional, for avatar
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivePromotion {
        private String id;
        private String code;
        private double discountPercentage;
        private String validUntil;
    }
}