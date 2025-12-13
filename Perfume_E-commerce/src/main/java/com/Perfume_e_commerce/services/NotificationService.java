package com.Perfume_e_commerce.services;

import com.Perfume_e_commerce.Repositories.NotificationRepository;
import com.Perfume_e_commerce.models.marketing.Notification;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(new ObjectId(userId));
    }

    public Notification markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(String userId) {
        List<Notification> notifs = notificationRepository.findByUserIdOrderByCreatedAtDesc(new ObjectId(userId));
        notifs.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifs);
    }

    public void createNotification(String userId, String message, String type) {
        Notification notification = new Notification();

        notification.setUserId(new ObjectId(userId));
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
