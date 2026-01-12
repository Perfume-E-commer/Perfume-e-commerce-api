package com.Perfume_e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {
    private long totalOrders30d;
    private double revenue30d;
    private long lowStockCount;
    private long activeCustomers;

    private List<DailySalesData> salesChart;

    private List<RecentOrder> recentOrders;
    private List<LowStockItem> lowStockItems;
    private List<ActivePromotion> activePromotions;

}