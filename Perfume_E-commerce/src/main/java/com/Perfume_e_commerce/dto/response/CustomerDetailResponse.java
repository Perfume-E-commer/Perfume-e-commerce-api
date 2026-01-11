package com.Perfume_e_commerce.dto.response;

import com.Perfume_e_commerce.models.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDetailResponse {
    private User customer;
    private List<OrderHistoryItem> orders;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderHistoryItem {
        private String id;
        private String orderNumber;
        private String date;
        private double total;
        private String status;
        private int itemCount;
    }
}