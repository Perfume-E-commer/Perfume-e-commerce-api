package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.order.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
}
