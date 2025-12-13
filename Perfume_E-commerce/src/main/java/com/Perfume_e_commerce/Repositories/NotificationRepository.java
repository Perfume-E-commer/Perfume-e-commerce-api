package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.marketing.Notification;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(ObjectId userId);
    List<Notification> findByIsReadFalse();

}
