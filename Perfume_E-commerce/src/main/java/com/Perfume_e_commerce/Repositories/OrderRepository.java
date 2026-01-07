package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);

    @Query("{ '$or': [ " +
            "{ 'orderNumber': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'userEmail': { '$regex': ?0, '$options': 'i' } } " +
            "] }")
    Page<Order> findByOrderNumberContainingIgnoreCase(String orderNumber, Pageable pageable);

    List<Order> findByUserEmail(String userEmail);
}
