package com.Perfume_e_commerce.Repositories;

import com.Perfume_e_commerce.models.inventory.InventoryLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLogRepository extends MongoRepository<InventoryLog, String> {
    List<InventoryLog> findByProductIdOrderByCreatedAtDesc(String productId);
}
