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
            "{ 'userEmail': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'shippingAddress.fullName': { '$regex': ?0, '$options': 'i' } } " +
            "] }")
    Page<Order> searchOrders(String keyword, Pageable pageable);

    Page<Order> findByOrderNumberContainingIgnoreCase(String orderNumber, Pageable pageable);

    List<Order> findByUserEmail(String userEmail);

    @Query("SELECT SUM(o.total) FROM Order o")
    Double getTotalRevenue();

    @Query("SELECT COUNT(DISTINCT o.user) FROM Order o")
    Long countPayingCustomers();

    @Query(value = "SELECT COUNT(*) FROM (SELECT user_id FROM orders GROUP BY user_id HAVING COUNT(*) > 1) as repeat_customers", nativeQuery = true)
    long countReturningCustomers();

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT o.user.id, COUNT(o), SUM(o.total), MAX(o.createdAt) FROM Order o GROUP BY o.user.id")
    List<Object[]> getUserOrderStats();
}
