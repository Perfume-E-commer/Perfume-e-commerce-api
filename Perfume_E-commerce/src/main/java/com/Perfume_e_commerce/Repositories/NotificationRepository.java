package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.marketing.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findByIsReadFalse();

}
