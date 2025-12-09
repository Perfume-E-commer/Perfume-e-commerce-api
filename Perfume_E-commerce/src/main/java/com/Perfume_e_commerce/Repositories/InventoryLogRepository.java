package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.inventory.InventoryLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InventoryLogRepository extends MongoRepository<InventoryLog, String> {
    List<InventoryLog> findByProductIdOrderByCreatedAtDesc(String productId);
}
